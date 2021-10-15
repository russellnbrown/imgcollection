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
 // It can use a simple or multithreaded approach.


// constructor. set the singleton
ImgCollectionBuilder::ImgCollectionBuilder(CreateType ct)
{
	createType = ct;
	//instance = this;
	ic = new ImgCollection();

	// Build & Search can be multithreaded, find out how many  based on the number of
	//  processors/cores in the machine. 
	
	if (createType == CREATETHREADS)
	{
		numThreads = std::thread::hardware_concurrency() * 2; // *2 seems to work well, more or less is slower
		if (numThreads <= 0)
			numThreads = 2;
	}
	else
		numThreads = 0;
}

void ImgCollectionBuilder::Save()
{
	ic->Save(saveTo);
}

// initCreate. only called from create, creates the image encoding threads
void ImgCollectionBuilder::initCreate()
{
	// flag used to end threads when needed
	running = true;

	// start each encoder if configured
	for (int t = 0; t < numThreads; t++)
	{
		RunThreadInfo *rti = new RunThreadInfo();
		rti->id = t;
		rti->trd = thread(&ImgCollectionBuilder::imgProcessingThread, this, rti);
		threads.push_back(rti);
		st.incThreads();
	}
	
}


// The 'image processing' thread
void ImgCollectionBuilder::imgProcessingThread(RunThreadInfo *ri)
{
	logger::info("Thread " + to_string(ri->id) + " running");
	ImageInfo *current = nullptr;
	int tp = 0;

	//
	// loop round reading items to process off the threadFeeder queue (populated by the fileWalker )
	//
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
		// nothing to do, have a nap and try again
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


// Two ways to use the builder, either Create ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
void ImgCollectionBuilder::Create(fs::path _top, fs::path path, fs::path _saveTo)
{
	saveTo = _saveTo;
	ic->top = _top;
	ic->stop = ic->top.string();
	ImgUtils::Replace(ic->stop, "\\", "/");

	initCreate();
	// call file walker with the 'top' directory
	walkFiles(path);
}

uint32_t ImgCollectionBuilder::pathsplit(const fs::path d, string &dirpart, string &filepart, time_t &lastMod)
{
	try
	{
		lastMod = 0;
		struct stat result;
		if (stat(d.string().c_str(), &result) == 0)
			lastMod = result.st_mtime;


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
	dirpart = dirpart.substr(ic->top.string().length());

	if ( dirpart.empty() || dirpart[0] != '/' )
		dirpart = "/" + dirpart;

	return ImgUtils::GetHash(dirpart);
}

//
// walkFiles
//
// The main 'Create' method. Called initially from the 'top' directory
// we then loop through files in that dir, if we find a directory, call ourselves
// recursivly. If we find a file, call processFile to deal with it ( in 
// imgProcessingThread )
//
bool ImgCollectionBuilder::walkFiles(fs::path dir)
{

	string dirpart;
	string filepart;
	time_t lastmod = 0;

	// add 'top' directory to the collection 
	uint32_t dirHash = pathsplit(dir, dirpart, filepart, lastmod);
	ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
	st.incDirs();

	// loop for all its files/subdirs
	int fcntr = 0;
	for (fs::directory_entry de : fs::recursive_directory_iterator(dir))
	{
		string dirpart;
		string filepart;

		// get crc32 of the directory path
		uint32_t dirHash = pathsplit(de, dirpart, filepart, lastmod);
		if (dirHash == 0)
		{
			st.incNameErrors();
			continue;
		}

		// givee occasional feedback
		if (++fcntr % 5000 == 0)
		{
			logger::info("At: " + st.progressStr());
		}

		// if its a directory, create a DirItem and add to collection
		if (filepart.empty())
		{
			st.incDirs();
			ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
			logger::debug("Dir is " + dirpart);
		}
		else
		{
			// if its an image file, create a ImageInfo and process it
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

	// wait for image processing threads to stop
	waitOnProcessingThreads();

	return true;
}

void ImgCollectionBuilder::waitOnProcessingThreads()
{
	logger::info("Running set to false");
	running = false;

	if (createType == CREATETHREADS)
	{
		MSNOOZE(10); // allow any thread to pick up final task if set. there is a better way to do this... 
		for (list<RunThreadInfo*>::iterator rt = threads.begin(); rt != threads.end(); rt++)
		{
			(*rt)->trd.join();
			logger::debug("Finished " + to_string((*rt)->id) + " has finished normally.");
		}
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
	// process an image file
	
	try
	{
		// if using threads, add the ImageInfo to the thread feeder queue, 
		// a thread will pick it off when it is ready
		if (createType == CREATETHREADS)
		{
			// if too many encodes in Q just wait
			while (threadFeeder.size() > (threads.size() * 10))
				MSNOOZE(10);

			// add to queue (protected as threads will access and remove items )
			tflock.lock();
			threadFeeder.push_back(ii);
			tflock.unlock();
		}
		else
		{
			//otherwise, just do what the thread would have done
			if (ImgUtils::GetImageInfo(ii))
				processItemResult(ii);
			else
				st.incErrors();
		}

	
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

//
// Save
//
// This saves the ImgCollection as three seperate files, dirss, files & images
//



