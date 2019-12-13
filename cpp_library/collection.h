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



 //
 // CollectionDirItem 
 //
 // holds information about a directory in the set and some helpers
 //
class CollectionDirItem
{
public:
	CollectionDirItem(HKey h, string p)
	{
		path = p;
		hash = h;
	}
	~CollectionDirItem()
	{
	}
	HKey		hash;		// csc32 of directory path ( used as key by FileItem )
	string		path;		// the path ( relative to collection top )

	// toSave - create a string suitable for saving to a dir file
	inline string toSave()
	{
		stringstream s;
		s << hash << "," << path;
		return s.str();
	}

	// return a DirItem by parsing a line from a dir file
	static inline unique_ptr<CollectionDirItem> fromSave(string s)
	{
		string dir, hash;
		std::istringstream iss(s);
		std::getline(iss, hash, ',');
		std::getline(iss, dir, ',');
		HKey dhash = strtol(hash.c_str(), nullptr, 10);
		return  make_unique<CollectionDirItem>(dhash, dir);
	}

	// toStr 
	inline string toStr()
	{
		ostringstream ret;
		ret << "CollectionDirItem[hash=";
		ret << hex << hash;
		ret << ",path=";
		ret << path;
		ret << "]";
		return  ret.str();
	}

	inline string toDBSave()
	{
		ostringstream ret;
		ret << hash;
		ret << ",'";
		ret << path;
		ret << "'";
		return  ret.str();
	}
	//"INSERT INTO dirs VALUES(1,'/temp')"
};


//
// CollectionFileItem 
//
// holds information about a file in the set and some helpers
//
class CollectionFileItem
{
public:
	CollectionFileItem(HKey dhash, HKey crc, string name)
	{
		this->dhash = dhash;
		this->crc = crc;
		this->name = name;
	}
	HKey	dhash;			// the key to CollectionDirItems for our directory
	HKey	crc;			// crc of the file 
	string	name;			// name of the file

	~CollectionFileItem()
	{
	}

	// toStr 
	string	toStr()
	{
		stringstream ss;
		ss << "CollectionFileItem[name=" << name << ", crc=" << crc << ", dhash=" << name << "]";
		return ss.str();
	}

	// toSave - create a string suitable for saving to a file file
	string	toSave()
	{
		stringstream ss;
		ss << dhash << "," << crc << "," << name;
		return ss.str();
	}
	inline string toDBSave()
	{
		ostringstream ret;
		ret << dhash;
		ret << ",";
		ret << crc;
		ret << ",'";
		ret << name;
		ret << "'";
		return  ret.str();
	}

	// return a FileItem by parsing a line from a file file
	static unique_ptr<CollectionFileItem> fromSave(string& s)
	{
		string crc, name, hash;
		std::istringstream iss(s);
		std::getline(iss, hash, ',');
		std::getline(iss, crc, ',');
		std::getline(iss, name, ',');
		HKey dhash = strtoll(hash.c_str(), nullptr, 10);
		HKey lcrc = strtoll(crc.c_str(), nullptr, 10);
		return make_unique<CollectionFileItem>(dhash, lcrc, name);
	}

};


//
// CollectionImageItem 
//
// holds information about an image in the collection and some helpers
//
class CollectionImageItem
{
public:

	// constructor. // NO ** tmb ** is moved into object. can't be used on return
	CollectionImageItem(HKey crc, ThumbVec &tmb)
	{
		this->crc = crc;
		this->thumb = tmb;

	}

	CollectionImageItem(HKey crc, int8_t* tmb)
	{
		this->crc = crc;
		this->thumb.reserve(TNMEM);
		this->thumb.insert(this->thumb.begin(), tmb, tmb + TNMEM);
	}

	HKey			crc;		// the crc of the image file contents
	ThumbVec		thumb;		// rgb tuples forming a thumbnail of image

	~CollectionImageItem()
	{
	}

	// return a useful debug string 
	string	toStr()
	{
		stringstream ss;
		ss << "SetItemImage[crc=" << crc << "]";
		return ss.str();
	}

	inline string toDBSave()
	{
		ostringstream ret;
		ret << crc;
		ret << ",'";
		ret << "'";
		return  ret.str();
	}
};

typedef unique_ptr<CollectionDirItem> DirItemUPtr;
typedef unique_ptr<CollectionFileItem> FileItemUPtr;
typedef shared_ptr<CollectionImageItem> ImageItemSPtr;

typedef list<DirItemUPtr> DirItemUPtrList;
typedef list<FileItemUPtr> FileItemUPtrList;
typedef map<HKey, ImageItemSPtr> ImageItemSPtrMap;
typedef vector<ImageItemSPtr> ImageItemSPtrVec;

//
// collection
//
// This is the collection object. It holds all the dirs, files & images
// in the collection. It also provides methods to load/save from disk or database
//

class icCollection
{
public:

	icCollection();
	~icCollection();

public:

	fs::path			top;		// the top level directory. all set directories relative to this
	DirItemUPtrList		dirs;		// simple lists for DirItem & FileItem
	FileItemUPtrList	files;		 
	ImageItemSPtrMap	imageMap;	// hash table for ImageItem to check duplicates etc
	ImageItemSPtrVec	images;		// vector of images for quick iteration in searches (load only)

};

typedef unique_ptr<icCollection> CollUPtr;
