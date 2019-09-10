#pragma once

class ImgCollection
{
public:

	// The 'ImgCollection'. these 3 structures form the ImgCollection. 
	// These are saved/loaded to a set of files on disk

	list<ImgCollectionDirItem*> dirs;
	list<ImgCollectionFileItem*> files;
	map<int64_t, ImgCollectionImageItem*> images;

};