/*
 * Copyright (C) 2019 russell brown
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


#include "cpp_library.h"
#include "cpp_library_internal.h"

SearchThreadInfo::SearchThreadInfo(int x)
{
	tix = x;
}

icSearch::icSearch(unique_ptr<icCollection>& _coll,  icUtils::SearchAlgo algo) : coll(_coll), istype(algo)
{
	// this is the image we are looking for
	searchItem = nullptr;

	numThreads = std::thread::hardware_concurrency() * 2;
	if (numThreads <= 0) // just in case above dosn't return a correct value
		numThreads = 2;
	logger::info("Search, Number of threads in use: " + to_string(numThreads));


	switch (istype)
	{
	case icUtils::SearchAlgo::Assembler: logger::info("Closeness calculation is ASSEMBLER"); break;
	case icUtils::SearchAlgo::Luma: logger::info("Closeness calculation is LUMA"); break;
	case icUtils::SearchAlgo::Simple: logger::info("Closeness calculation is SIMPLE"); break;
	case icUtils::SearchAlgo::Mono: logger::info("Closeness calculation is MONO"); break;
	}

}

// Find. Searches the imgcollection to find a matching image. We calculate
// a 'closeness' value for each image and keep the results in a treeset 
// so that we can return them in order
list<matchingItem*> icSearch::Find(fs::path search)
{
	initFind();

	// Create an ImageInfo of the file to search and get
	// file bytes and thumb
	searchItem = make_unique<ImageInfo>();
	searchItem->de = search;
	icUtils::GetImageInfo(searchItem);

	// go through all images and calculate a closeness
	Timer::start();

	int lastImageIx = coll->images.size();
	int curImage = 0;
	int perThread = (int)((float)lastImageIx / (float)numThreads);
	int threadIx = 0;

	// The srchThreads vector holds a start & end index of the images it is responsible 
	// for. Start these threads
	for (vector<shared_ptr<SearchThreadInfo>>::iterator sti = srchThreads.begin(); sti != srchThreads.end(); sti++)
	{
		(*sti)->tix = threadIx++;
		// start with image curImage
		(*sti)->startix = curImage;
		if (threadIx == srchThreads.size() ) // last thread
			(*sti)->count = lastImageIx-curImage;
		else
			(*sti)->count = perThread;


		logger::info("Thread " + to_string((*sti)->tix) + " start=" + to_string((*sti)->startix) + " end=" + to_string((*sti)->count));
		curImage += perThread;
		(*sti)->trd = thread(&icSearch::tFind, this, (*sti));
	}

	// wait for them to finish and copy the threads results into the main result list
	for (vector<shared_ptr<SearchThreadInfo >> ::iterator sti = srchThreads.begin(); sti != srchThreads.end(); sti++)
	{
		(*sti)->trd.join();
		results.splice(results.end(), (*sti)->results, (*sti)->results.begin(), (*sti)->results.end());
	}
	
	//sort results
	results.sort([](const shared_ptr<SearchResult > first, const shared_ptr<SearchResult>  second)
		{
			return first->closeness < second->closeness;
		});

	Timer::stop("Calculating closeness");

	Timer::start();
	int findTop = 10;

	list<matchingItem*> rv;

	// search files for ones matching the closest image
	logger::info("Results:");
	for (ResultSPtrList::iterator r = results.begin(); r != results.end() && findTop-- > 0; r++)
	{
		matchingItem* mi = new matchingItem();
		mi->closeness = (*r)->closeness;
		for (FileItemUPtrList::iterator it = coll->files.begin(); it != coll->files.end(); ++it)
		{
			FileItemUPtr &f = *it;
			if (f->crc == (*r)->i->crc)
				mi->files.push_back(pathOf(f));
		}
		rv.push_back(mi);
	}

	Timer::stop("Find matched image files");

	return std::move(rv);

}

// tFind
// If using threads this is our thread to calc closeness

void icSearch::tFind(shared_ptr<SearchThreadInfo> sti)
{
	int pimage = 0;


	for( int e = sti->startix; e < sti->startix+sti->count; e++)
	{ 		
			shared_ptr<SearchResult>sr = make_shared<SearchResult>();

			sr->i = coll->images[e];

			if (searchItem->crc == coll->images[e]->crc)
				sr->closeness = 0; // identical images
			else
				sr->closeness = icUtils::GetCloseness(searchItem->thumb, coll->images[e]->thumb, istype);
			sti->results.push_back(sr);
			pimage++;
		}
	

	logger::debug("Thread " + to_string(sti->tix) + " searched " + to_string(pimage) + " images");
}


// compareCloseness. Used by the list sort to order by closeness
bool compareCloseness(const SearchResult* first, const SearchResult* second)
{
	return first->closeness < second->closeness;
}


void icSearch::initFind()
{
	// if using threads, create the SearchThreadInfo objects used
	// by the threads
	if (numThreads > 0)
	{
		for (int x = 0; x < numThreads; x++)
		{
			logger::debug("Creating search thread " + to_string(x));
			srchThreads.push_back(make_shared<SearchThreadInfo>(x));
		}
	}
	else
		logger::info("Not creating any search threads");
}

string icSearch::pathOf(unique_ptr<CollectionFileItem>& f)
{
	string path = coll->top.string();
	for (unique_ptr < CollectionDirItem>&d : coll->dirs)
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
