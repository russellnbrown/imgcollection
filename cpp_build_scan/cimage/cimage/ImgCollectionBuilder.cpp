/*
 * Copyright (C) 2018 russell brown
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


#include "cimage.h"


 // ImgCollectionBuilder. This is a helper class to build an Image Collection.
 // it is derived from SimpleFileVisitor as one of its functions will be
 // to scan all the image files in the 'top' directory

ImgCollectionBuilder *ImgCollectionBuilder::instance = nullptr;

// constructor. set the singleton
ImgCollectionBuilder::ImgCollectionBuilder()
{
	instance = this;
	ic = new ImgCollection();
}

// init. only called from create, creates the image encoding threads
void ImgCollectionBuilder::init()
{
	// flag used to end threads when needed
	running = true;

	// create a number of encoder threads based on the number of processors/cores
	// in the machine. We dont create a thread for each file as its scanned as we will 
	// run out of resources. 
	int nt = std::thread::hardware_concurrency();
	if (nt <= 0)
		nt = 2;
	
	// start each encoder
	for (int t = 0; t < nt; t++)
	{
		RunThreadInfo *rti = new RunThreadInfo();
		rti->id = t;
		rti->trd = thread(_run, rti);
		threads.push_back(rti);
		st.incThreads();
	}
	
}

// Load. loads the imgcollection from disk. each of the collections is read from its own
// file. 
// ** these may have been created my one of the other imagecollection implementations so
//    we cant use serialization **
void ImgCollectionBuilder::Load(fs::path set)
{
	string line;
	ifstream idir(set.string() + "/dirs.txt");
	getline(idir, line);

	top = fs::path(line);
	stop = line;
	if (!fs::exists(top))
		logger::fatal("Top dir " + line + " does not exist.");


	while (getline(idir, line))
	{
		ImgCollectionDirItem *d = ImgCollectionDirItem::fromSave(line);
		if (d != nullptr)
		{
			st.incDirs();
			ic->dirs.push_back(d);
		}
	}
	idir.close();
	

	ifstream ifile(set.string() + "/files.txt");
	while (getline(ifile, line))
	{
		ImgCollectionFileItem *f = ImgCollectionFileItem::fromSave(line);
		if (f != nullptr)
		{
			st.incFiles();
			ic->files.push_back(f);
		}
	}
	ifile.close();
	


	ifstream iimg(set.string() + "/images.bin", ios::binary);
	int64_t icrc;
	int8_t thumb[TNMEM];
	while (true)
	{
		if (iimg.read((char*)&icrc, sizeof(icrc)))
		{
			iimg.read((char*)thumb, TNMEM);
			ImgCollectionImageItem *sii = new ImgCollectionImageItem(icrc, thumb);
			ic->images[icrc]=sii;
			st.incImages();
		//	printf("CRC: %lld RGB: %2.2x %2.2x %2.2x\n", icrc, thumb[0]&0xFF, thumb[1] & 0xFF, thumb[2] & 0xFF);
		}
		else
			break;
	}
	iimg.close();
	
	logger::info(st.progressStr());

	return;
}

// compareCloseness. Used by the list sort to order by closeness
bool compareCloseness(const SearchResult *first, const SearchResult *second)
{
	return first->closeness < second->closeness;
}

// Find. Searches the imgcollection to find a matching image. We calculate
// a 'closeness' value for each image and keep the results in a treeset 
// so that we can return them in order
void ImgCollectionBuilder::Find(fs::path search)
{

	// Create an ImageInfo of the file to search and get
	// file bytes and thumb
	ImageInfo *ii = new ImageInfo();
	ii->de = search;
	ImgUtils::GetImageInfo(ii);

	//ofstream odir("debug.c.csv");
	
	std::list<SearchResult*> results;


	// go through all images and calculate a closeness
	Timer::start();
	for (map<int64_t, ImgCollectionImageItem*>::iterator it = ic->images.begin(); it != ic->images.end(); ++it)
	{
		int64_t icrc = it->first;
		ImgCollectionImageItem *f = it->second;
		SearchResult *sr = new SearchResult();
		// create a searchresult for this image and add to the list
		//stringstream ss;
		//ss << "img," << f->crc;
		sr->i = f;
		if (ii->crc == f->crc)
			sr->closeness = 0; // identical images
		else
		{
			sr->closeness = ImgUtils::GetCloseness(ii->thumb, f->thumb);// , &ss);
			//odir << ss.str() << endl;
		}
		results.push_back(sr);


	}
	//sort results
	
	
	results.sort([](const SearchResult *first, const SearchResult *second)
	{
		return first->closeness < second->closeness;
	});

	Timer::stop("Calculating closeness");

	Timer::start();
	int findTop = 10;
	int rank = 0;
	// search files for ones matching the closest image
	logger::info("Results:");
	for (list<SearchResult*>::iterator r = results.begin(); r != results.end() && findTop-- > 0; r++, rank++)
	{
		SearchResult *sr = *r;
		logger::raw("   Img:" + to_string(sr->i->crc) + ", Closeness: " + to_string(sr->closeness/10.0) + ", Files:-");
		for (list<ImgCollectionFileItem*>::iterator it = ic->files.begin(); it != ic->files.end(); ++it)
		{
			ImgCollectionFileItem *f = *it;
			if (f->crc == sr->i->crc)
				logger::raw("      File: " + pathOf(f));
		}
	}
	Timer::stop("Find matched image files");


}

void ImgCollectionBuilder::_run(RunThreadInfo *ri)
{
	ImgCollectionBuilder::instance->run(ri);
}

// The 'immage processing' thread
void ImgCollectionBuilder::run(RunThreadInfo *ri)
{
	logger::info("Thread " + to_string(ri->id) + " running");
	ImageInfo *current = nullptr;
	int tp = 0;

	while (true)
	{
		// get next job
		tflock.lock();
		int ts = threadFeeder.size();
		if (ts > 0)
		{
			current = threadFeeder.front();
			threadFeeder.pop_front();
		}
		tflock.unlock();
		if (current == nullptr)
		{
			if (!running)
				break;
			MSNOOZE(10);
			continue;
		}

		tp++;
		logger::debug("Processing " + current->filepart + " in thread " + to_string(ri->id) );
		try
		{
			if ( ImgUtils::GetImageInfo(current) ) 
				processItemResult(current);
			else
				st.incErrors();
		}
		catch (std::runtime_error &e)
		{
			logger::error("Error processing " + current->filepart + ", " + string(e.what()));
		}
		catch (std::exception &e)
		{
			logger::error("Error processing " + current->filepart + ", " + string(e.what()));
		}
		current = nullptr;
	}
	logger::info("Thread " + to_string(ri->id) + " stopping, it processed " + to_string(tp) + " images.");
}


// Two ways to use the builder, either Craete ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
void ImgCollectionBuilder::Create(fs::path path)
{
	top = path;
	stop = top.string();
	ImgUtils::Replace(stop, "\\", "/");

	init();
	walkFiles(path);
}

int64_t ImgCollectionBuilder::pathsplit(const fs::path d, string &dirpart, string &filepart)
{
	try
	{
		if (fs::is_regular_file(d))
		{
			filepart = d.filename().string();
			dirpart = d.parent_path().string();
		}
		else if (fs::is_directory(d))
		{
			filepart = "";
			dirpart = d.string();
		}
		else
			return 0;
	}
	catch (std::exception e)
	{
		logger::warn("Ignore ");
		return 0;
	}

	ImgUtils::Replace(dirpart, "\\", "/");
	dirpart = dirpart.substr(top.string().length());

	if ( dirpart.empty() || dirpart[0] != '/' )
		dirpart = "/" + dirpart;

	return ImgUtils::GetHash(dirpart);
}


bool ImgCollectionBuilder::walkFiles(fs::path dir)
{

	string dirpart;
	string filepart;
	int64_t dirHash = pathsplit(dir, dirpart, filepart);
	ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart));
	st.incDirs();

	int fcntr = 0;

	for (fs::directory_entry de : fs::recursive_directory_iterator(dir))
	{
		string dirpart;
		string filepart;
		int64_t dirHash = pathsplit(de, dirpart, filepart);
		if (dirHash == 0)
		{
			st.incNameErrors();
			continue;
		}

		if (++fcntr % 100 == 0)
		{
			logger::info("At: " + st.progressStr());
		}

		if (filepart.empty())
		{
			st.incDirs();
			ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart));
			logger::debug("Dir is " + dirpart);
		}
		else
		{
			st.incFiles();
			if (ImgUtils::IsImageFile(de.path()))
			{
				ImageInfo *ii = new ImageInfo();
				ii->de = de;
				ii->dirhash = dirHash;
				ii->filepart = filepart;
				processItem(ii);
			}
		}
	}

	MSNOOZE(10); // allow any thread to pick up task if set. there is a better way to do this... 
	logger::info("Running set to false");
	running = false;
	waitOnProcessingThreads();

	return true;
}

void ImgCollectionBuilder::waitOnProcessingThreads()
{


	for (list<RunThreadInfo*>::iterator rt = threads.begin(); rt != threads.end(); rt++)
	{
		(*rt)->trd.join();
		logger::debug("Finished " + to_string((*rt)->id) + " has finished normally.");
	}


}
	
void ImgCollectionBuilder::processItemResult(ImageInfo *ii)
{
	//logger::info("Results " + ii->filepart);
	string err;

	st.addBytes(ii->size);
	{
		scoped_lock sl(llock);
		try
		{
			ic->files.push_back(new ImgCollectionFileItem(ii->dirhash, ii->crc, ii->filepart));
			if (ic->images.find(ii->crc) == ic->images.end())
			{
				ic->images[ii->crc] = new ImgCollectionImageItem(ii->crc, ii->thumb);
				st.incImages();
			}
			else
				st.incDuplicates();
		}
		catch (exception &e)
		{
			st.incErrors();
			err = "Could not gen image " + ii->filepart + string(e.what());
		}
	}

	delete ii;


	if (err.length() > 0)
		logger::error(err);

}

void ImgCollectionBuilder::processItem(ImageInfo *ii)
{
	
	try
	{

		// if too many encodes in Q just wait
		while( threadFeeder.size() > (threads.size()*2) )
			MSNOOZE(10);

		// add job to queue
		//logger::info("Enqueue " + ii->filepart);
		tflock.lock();
		threadFeeder.push_back(ii);
		tflock.unlock();

	
	}
	catch (runtime_error &e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}
	catch (exception &e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}

}

bool ImgCollectionBuilder::Save(fs::path dir)
{
	string dirpart;
	string filepart;

	string tstr = top.string();
	ImgUtils::Replace(tstr, "\\", "/");

	ofstream odir(dir.string()+"/dirs.txt");
	odir <<  tstr << endl;
	for (list<ImgCollectionDirItem*>::iterator it = ic->dirs.begin(); it != ic->dirs.end(); ++it)
	{
		ImgCollectionDirItem *d = *it;
		odir << d->toSave() << endl;
	}
	odir.close();

	ofstream ofile(dir.string() + "/files.txt");
	for (list<ImgCollectionFileItem*>::iterator it = ic->files.begin(); it != ic->files.end(); ++it)
	{
		ImgCollectionFileItem *f = *it;
		ofile << f->toSave() << endl;
	}
	ofile.close();

	ofstream oimg(dir.string() + "/images.bin", ios::out | ios::binary);
	for (map<int64_t,ImgCollectionImageItem*>::iterator it = ic->images.begin(); it != ic->images.end(); ++it)
	{
		int64_t icrc = it->first;
		ImgCollectionImageItem *f = it->second;
		oimg.write((char*)&icrc, sizeof(icrc));
		oimg.write((char*)f->thumb, TNMEM);		
	}
	oimg.close();


	return true;
}

string ImgCollectionBuilder::pathOf(ImgCollectionFileItem *f)
{
	string path = stop;
	for (ImgCollectionDirItem *d : ic->dirs)
	{
		if (d->hash == f->dhash)
		{
			path.append(d->path);
			break;
		}
	}
	path.append("/");
	path.append(f->name);
	return path;
}

