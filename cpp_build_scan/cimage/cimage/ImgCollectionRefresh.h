
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

 //
 // ImgCollectionBuilder
 //
 // This is a helper class to build an Image Collection and to
 // provide various methods to access it
 //

class ImgCollectionRefresh
{


private:

	ImgCollection* ic = nullptr;			// The ImgCollection

	bool running;							// used to stop image processing threads
	Stats st;								// stats on processing
	list<RunThreadInfo*> threads;			// image processing threads
	list<ImageInfo*> threadFeeder;			// queue for feeding files to image processing threads
	mutex tflock;							// lock for above
	mutex llock;							// lock for changing content of set lists
	list<SearchResult*> results;			// in searches, the results of comparisons
	ImageInfo* searchItem;					// in searches, the image being searched for
	int numThreads;							// number of threads we can use for threaded operations
	fs::path saveTo;

public:
	ImgCollectionRefresh();

public:

	void Create(fs::path top, fs::path path, fs::path _saveTo);// create the ImgCollection 
	void Save();

private:
	bool walkFiles(fs::path dir);			// iterates over files in a directory tree 
	int64_t pathsplit(const fs::path d, string& dir, string& file, time_t& lastMod);
	// split a file path into its dir & file (if any) componenets


	void processItem(ImageInfo* ii);		// process an image file
	void processItemResult(ImageInfo* ii);	// process result of above
	void waitOnProcessingThreads();			// wait for all image processing threads to stop
	void initCreate();							// initialize & create image processing threads
	void imgProcessingThread(RunThreadInfo* ri);			// image processing thread method


};

