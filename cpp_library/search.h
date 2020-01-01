#pragma once

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

typedef list<shared_ptr<SearchResult>> SharedResultList;

class SearchThreadInfo
{
public:
	SearchThreadInfo(int x);

public:
	thread trd;									// thread identifier
	int tix;									// thread index ( 1..n )
	SharedResultList	results;		// search results for this thread
	int startix;								// first image to search in the collection images vector
	int count;									// how many to search ( each thread has a different range )
};

typedef vector<shared_ptr<SearchThreadInfo>> SearchThreadsVec;
typedef shared_ptr<ImageInfo> ImageInfoSPtr;


class icSearch
{
public:
	icSearch(icCollection *_coll, icUtils::SearchAlgo algo);				// the search class

public:
	list<matchingItem*> Find(fs::path file); 
											// the find function. passed file to look for and algo to use

private:
	SharedResultList		results;		// all search results ( combine thread results )	
	icCollection			*coll;
	ImageInfoSPtr			searchItem;		// in searches, the image being searched for
	int						numThreads;		// number of threads we can use for threaded operations
	SearchThreadsVec		srchThreads;	// search threads
	void					tFind(shared_ptr<SearchThreadInfo>);
	void					initFind();
	string					pathOf(unique_ptr<CollectionFileItem>& f);	// full path of a file
	fs::path				setToLoad;
	icUtils::SearchAlgo	istype;			// closeness mode

};

