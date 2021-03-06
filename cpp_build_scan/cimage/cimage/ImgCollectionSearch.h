


// Threading Notes
// two ways to do multithreaded search 
// 1. SRCHMAP
//    set a thread index in the ImageCollectionItemImage as they are loaded into the image map, inc for
//    each new instance & back to 0 when numThreads is reached. In the search thread tFind, iterate through 
//    all images in the map and only process those where the index matches the index of the thread
//    ImgCollectionImageItem.tix == SearchThreadInfo.tix so each search thread only processes 1 in numThreads
//    images
//
// 2. SRCHLIST
//    add a list of ImageCollectionItemImage to the SearchThreadInfo. As images are loaded add ImageCollectionItemImage
//    to the list as well as the main map. In tFind, procedd that list rather than the main map.
//
// SRCHLIST is faster as each thread dosn't have to iterate through the whole map, but does use more space
// as the list duplicates pointers
//

//
// SearchThreadInfo
//
// Holds information about a search thread.
//
class SearchThreadInfo
{
public:
	SearchThreadInfo(int x);
 
	list<ImgCollectionImageItem*>  myItems;
	thread                         trd;
	std::list<SearchResult*>       results;
	int                            tix;
};

//
// ImgCollectionSearch
//
// Holds information about a search.
//
class ImgCollectionSearch 
{
public:
	enum SrchType { SRCHMAP, SRCHLIST, SRCHNOTHRD };

private:

	ImgCollection* ic = nullptr;			// The ImgCollection being searched
	
	Stats st;								// stats on processing
	fs::path top;							// the top level directory. all set directories relative to this
	string stop;							// string of above with '/' as file seperator
	list<SearchResult*> results;			// in searches, the results of comparisons
	ImageInfo* searchItem;					// in searches, the image being searched for
	int numThreads;							// number of threads we can use for threaded operations
	vector<SearchThreadInfo*> srchThreads;  // search threads

public:
	ImgCollectionSearch(SrchType,  ImgUtils::SearchType istype);

public:

	void Load(fs::path set);				// Load ImgCollection from file set
		void Find(fs::path search);			// search the ImgCollection for an image

private:

	void loadImages();						// Load images from file set
	void loadFiles();						// Load images from database set
	void loadDirs();						// Load directories from database set
	void tFind(SearchThreadInfo*);
	void initFind();
	string pathOf(ImgCollectionFileItem* f); // full path of a file
	fs::path setToLoad;
	SrchType srchType;						// search threading mode
	ImgUtils::SearchType istype;			// closeness mode

};

