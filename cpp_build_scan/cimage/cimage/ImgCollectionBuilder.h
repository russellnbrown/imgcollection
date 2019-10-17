
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


// ImgCollectionBuilder. This is a helper class to build an Image Collection and to
// provide various methods to access it

class SearchThreadInfo
{
public:
	list<ImgCollectionImageItem*> myItems;
	thread trd;
	std::list<SearchResult*> results;
};


class ImgCollectionBuilder
{
private:

	ImgCollection *ic = nullptr;			// The ImgCollection

	static ImgCollectionBuilder *instance;  // singleton access

	bool running;							// used to stop image processing threads
	Stats st;								// stats on processing
	list<RunThreadInfo*> threads;			// image processing threads
	list<ImageInfo*> threadFeeder;			// queue for feeding files to image processing threads
	mutex tflock;							// lock for above
	fs::path top;							// the top level directory. all set directories relative to this
	string stop;							// string of above with '/' as file seperator
	mutex llock;							// lock for changing content of set lists
	list<SearchResult*> results;			// in searches, the results of comparisons
	ImageInfo* searchItem;					// in searches, the image being searched for
	int numThreads;							// number of threads we can use for threaded operations
	vector<SearchThreadInfo*> srchThreads;  // search threads

public:
	ImgCollectionBuilder();

public:

	void Create(fs::path path);				// create the ImgCollection 
	void Load(fs::path set);				// Load ImgCollection from file set
	void Find(fs::path search);				// search the ImgCollection for an image
	bool Save(fs::path dir);				// save the ImgCollection to file set

private:
	bool walkFiles(fs::path dir);			// iterates over files in a directory tree 
	int64_t pathsplit(const fs::path d, string &dir, string &file);
											// split a file path into its dir & file (if any) componenets
	fs::path setToLoad;

	void loadImages();						// Load images from file set
	void loadFiles();						// Load images from database set
	void loadDirs();						// Load directories from database set
	void processItem(ImageInfo *ii);		// process an image file
	void processItemResult(ImageInfo *ii);	// process result of above
	void waitOnProcessingThreads();			// wait for all image processing threads to stop
	void initCreate();							// initialize & create image processing threads
	void imgProcessingThread(RunThreadInfo *ri);			// image processing thread method
	string pathOf(ImgCollectionFileItem *f); // full path of a file
	void tFind(SearchThreadInfo*);
	void initFind();

};

