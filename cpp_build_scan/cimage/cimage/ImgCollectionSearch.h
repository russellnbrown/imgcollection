#pragma once

class SearchThreadInfo
{
public:
	SearchThreadInfo(int x);

	list<ImgCollectionImageItem*> myItems;
	thread trd;
	std::list<SearchResult*> results;
	int tix = -1;
};




class ImgCollectionSearch
{
private:

	ImgCollection* ic = nullptr;			// The ImgCollection

	Stats st;								// stats on processing
	fs::path top;							// the top level directory. all set directories relative to this
	string stop;							// string of above with '/' as file seperator
	list<SearchResult*> results;			// in searches, the results of comparisons
	ImageInfo* searchItem;					// in searches, the image being searched for
	int numThreads;							// number of threads we can use for threaded operations
	vector<SearchThreadInfo*> srchThreads;  // search threads

public:
	ImgCollectionSearch();

public:

	void Load(fs::path set);				// Load ImgCollection from file set
	void Find(fs::path search);				// search the ImgCollection for an image

private:

	void loadImages();						// Load images from file set
	void loadFiles();						// Load images from database set
	void loadDirs();						// Load directories from database set
	void tFind(SearchThreadInfo*);
	void initFind();
	string pathOf(ImgCollectionFileItem* f); // full path of a file
	fs::path setToLoad;

};

