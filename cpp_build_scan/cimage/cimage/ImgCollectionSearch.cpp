#include "cimage.h"



SearchThreadInfo::SearchThreadInfo(int x)
{
	tix = x;
}

//
// ImgCollectionSearch
// ImgCollectionSearch is a class to load and search an image database.
//
ImgCollectionSearch::ImgCollectionSearch(SrchType s)
{
	// this is the image we are looking for
	searchItem = nullptr;
 
	srchType = s;

	// ic holds the structures constituting the database
	ic = new ImgCollection();

	// calculate how many threads to use for search. We use number of cores *2
	// as this seems to work well, fewer or more is slower
	// ** to prevent multithreading, set this to 0
  if ( srchType == SRCHNOTHRD )
    numThreads=0;
  else
  {
	  numThreads = std::thread::hardware_concurrency() * 2; 
	  if (numThreads <= 0) // just in case above dosn't return a correct value
		  numThreads = 2;
   }
   logger::info("Number of search threads in use: " + to_string(numThreads) );

}

// Find. Searches the imgcollection to find a matching image. We calculate
// a 'closeness' value for each image and keep the results in a treeset 
// so that we can return them in order
void ImgCollectionSearch::Find(fs::path search)
{

	// Create an ImageInfo of the file to search and get
	// file bytes and thumb
	searchItem = new ImageInfo();
	searchItem->de = search;
	ImgUtils::GetImageInfo(searchItem);

	// go through all images and calculate a closeness
	Timer::start();

	// If not using threads, just go through images and calculate a closeness, put result in 'results'
	if (srchThreads.size() == 0)
	{
		logger::info("Not using threads for search");
		for (map<int64_t, ImgCollectionImageItem*>::iterator it = ic->images.begin(); it != ic->images.end(); ++it)
		{
			int64_t icrc = it->first;
			ImgCollectionImageItem* f = it->second;
			SearchResult* sr = new SearchResult();
			// create a searchresult for this image and add to the list
			sr->i = f;
			if (searchItem->crc == f->crc)
				sr->closeness = 0; // identical images
			else
			{
				sr->closeness = ImgUtils::GetCloseness(searchItem->thumb, f->thumb);
			}
			results.push_back(sr);
		}
	}
	else
	{
		// If using threads, the srchThreads vector holds a number of SearchThreadInfo, which contain 
		// a subset of images to check ( see 'Load' ). Start these threads
		for (vector<SearchThreadInfo*>::iterator sti = srchThreads.begin(); sti != srchThreads.end(); sti++)
			(*sti)->trd = thread(&ImgCollectionSearch::tFind, this, (*sti));

		// wait for them to finish and copy the threads results into the main result list
		for (vector<SearchThreadInfo*>::iterator sti = srchThreads.begin(); sti != srchThreads.end(); sti++)
		{
			(*sti)->trd.join();
			results.splice(results.end(), (*sti)->results, (*sti)->results.begin(), (*sti)->results.end());
		}
	}

	//sort results
	results.sort([](const SearchResult* first, const SearchResult* second)
		{
			return first->closeness < second->closeness;
		});

	Timer::stop("Calculating closeness");

	Timer::start();
	int findTop = 10;
	// search files for ones matching the closest image
	logger::info("Results:");
	for (list<SearchResult*>::iterator r = results.begin(); r != results.end() && findTop-- > 0; r++ )
	{
		SearchResult* sr = *r;
		logger::raw("   Img:" + to_string(sr->i->crc) + ", Closeness: " + to_string(sr->closeness / 10.0) + ", Files:-");
		for (list<ImgCollectionFileItem*>::iterator it = ic->files.begin(); it != ic->files.end(); ++it)
		{
			ImgCollectionFileItem* f = *it;
			if (f->crc == sr->i->crc)
				logger::raw("      File: " + pathOf(f));
		}
	}
	Timer::stop("Find matched image files");


}

// tFind
// If using threads this is our thread to calc closeness

void ImgCollectionSearch::tFind(SearchThreadInfo* sti)
{
	int pimage = 0;
	// this is the search thread main loop. We either iterate over the list of ImgCollectionImageItem in myItems
	// (SRCHLIST) OR we iterate over the whole map but only process the ImgCollectionImageItem if the index matches
	// ours ( See Load )
	if (srchType == SRCHLIST)
	{
		logger::info("Thread " + to_string(sti->tix) + " searching using list");
		for (list<ImgCollectionImageItem*>::iterator it = sti->myItems.begin(); it != sti->myItems.end(); ++it)
		{
			pimage++;
			ImgCollectionImageItem* f = *it;
			SearchResult* sr = new SearchResult();
			// create a searchresult for this image and add to the list
			sr->i = f;
			if (searchItem->crc == f->crc)
				sr->closeness = 0; // identical images
			else
			{
				sr->closeness = ImgUtils::GetCloseness(searchItem->thumb, f->thumb);
			}
			sti->results.push_back(sr);
		}
	}
	else // SRCHMAP
	{
		logger::info("Thread " + to_string(sti->tix) + " searching using map");
		for (map<int64_t, ImgCollectionImageItem*>::iterator it = ic->images.begin(); it != ic->images.end(); ++it)
		{
			ImgCollectionImageItem* f = it->second;
			if (f->tix != sti->tix)
				continue;
			pimage++;
			SearchResult* sr = new SearchResult();
			// create a searchresult for this image and add to the list
			sr->i = f;
			if (searchItem->crc == f->crc)
				sr->closeness = 0; // identical images
			else
			{
				sr->closeness = ImgUtils::GetCloseness(searchItem->thumb, f->thumb);
			}
			sti->results.push_back(sr);
		}
	}
	logger::info("Thread " + to_string(sti->tix) + " searched " + to_string(pimage) + " images");
}

// Load. loads the imgcollection from disk. each of the collections is read from its own
// file. 
// ** these may have been created my one of the other imagecollection implementations so
//    we cant use serialization **
void ImgCollectionSearch::Load(fs::path set)
{
	initFind();

	
	setToLoad = set;
	if (srchType == SRCHNOTHRD)
	{
		logger::info("Loading with no threads");

		loadDirs();
		loadFiles();
		loadImages();
	}
	else
	{
		logger::info("Loading with threads");
		// We can split loading into 3 threads, one to read each
		thread dt = thread(&ImgCollectionSearch::loadDirs, this);
		thread ft = thread(&ImgCollectionSearch::loadFiles, this);
		thread it = thread(&ImgCollectionSearch::loadImages, this);
		dt.join();
		ft.join();
		it.join();
	}

	logger::info(st.progressStr());

	return;
}



void ImgCollectionSearch::loadDirs()
{
	string line;
	ifstream idir(setToLoad.string() + "/dirs.txt");
	getline(idir, line);

	top = fs::path(line);
	stop = line;
	if (!fs::exists(top))
		logger::fatal("Top dir " + line + " does not exist.");


	while (getline(idir, line))
	{
		ImgCollectionDirItem* d = ImgCollectionDirItem::fromSave(line);
		if (d != nullptr)
		{
			st.incDirs();
			ic->dirs.push_back(d);
		}
	}
	idir.close();

	return;
}

void ImgCollectionSearch::loadFiles()
{
	string line;

	ifstream ifile(setToLoad.string() + "/files.txt");
	while (getline(ifile, line))
	{
		ImgCollectionFileItem* f = ImgCollectionFileItem::fromSave(line);
		if (f != nullptr)
		{
			st.incFiles();
			ic->files.push_back(f);
		}
	}
	ifile.close();


	return;
}

void ImgCollectionSearch::loadImages()
{
	ifstream iimg(setToLoad.string() + "/images.bin", ios::binary);
	int64_t icrc;
	int8_t thumb[TNMEM];
	int stx = 0;
	while (true)
	{
		if (iimg.read((char*)&icrc, sizeof(icrc)))
		{
			iimg.read((char*)thumb, TNMEM);
			ImgCollectionImageItem* sii = new ImgCollectionImageItem(icrc, thumb);
			// store in image map
			ic->images[icrc] = sii;
			// if we are using threads to do the search, stx incs from 0 to numThreads then back to 0 and repeats, we either
			// use this in the ImgCollectionImageItem as an indicatior of the thread instance that is to process it if
			// each thread iterates the whole map (SRCHMAP) OR we also add the ImgCollectionImageItem to the a list in
			// the appropriate thread instance (SRCHLIST). Latter is faster but uses more memory as each ImgCollectionImageItem
			// is duplicated in the map & list.
			if (srchType == SRCHMAP)
				sii->tix = stx;
			else if (srchType == SRCHLIST)
				srchThreads[stx]->myItems.push_back(sii);

			if (++stx == numThreads) // wrap around
				stx = 0;

			st.incImages();
		}
		else
			break;
	}
	iimg.close();

	return;
}

// compareCloseness. Used by the list sort to order by closeness
bool compareCloseness(const SearchResult* first, const SearchResult* second)
{
	return first->closeness < second->closeness;
}


void ImgCollectionSearch::initFind()
{
	// if using threads, create the SearchThreadInfo objects used
	// by the threads
	if (numThreads > 0)
	{
		for (int x = 0; x < numThreads; x++)
		{
			logger::info("Creating search thread " + to_string(x));
			srchThreads.push_back(new SearchThreadInfo(x));
		}
	}
	else
		logger::info("Not creating any search threads");
}

string ImgCollectionSearch::pathOf(ImgCollectionFileItem* f)
{
	string path = stop;
	for (ImgCollectionDirItem* d : ic->dirs)
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
