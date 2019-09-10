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

#pragma once


// ImageInfo holds information about an image file 
class ImageInfo
{
public:
	int32_t size;		// file size
	int8_t *thumb;		// its thumbnail
	int8_t *bytes;		// file bytes
	int64_t crc;		// crc of bytes
	fs::path de;		// path of file
	int64_t dirhash;	// hash to dir
	string filepart;	// file name

	ImageInfo()
	{
		thumb = 0;
		bytes = 0;
		size = 0;
		crc = 0;
		dirhash = 0;
		filepart = "";
	}

	~ImageInfo()
	{
		delete thumb;
		delete bytes;
	}
	string toStr()
	{
		return "ImageInfo for " + filepart;
	}
};

// Holds info about an file searched in an image search. Closeness is a measure of how 
// similar it is to the searched image
class ImgCollectionImageItem;
class SearchResult
{
public:
	ImgCollectionImageItem *i;
	double closeness;
};

// bits 'n bobs 
class ImgUtils
{
public:
	static int64_t GetHash(string &path);			// get crc of a file on disk
	static int64_t GetHash(int8_t *bytes, int len);	// get crc hash of a files bytes
	static bool IsImageFile(fs::path fileName);		// does file have an image extension
	static fs::path Cwd();							// current dir
	static void Replace(string& source, string const& find, string const& replace);
													// replace strings in a string with a string
	static bool GetImageInfo(ImageInfo *ii);		// get other image info ( crc & thumb ) of a file in an image info
	static double GetCloseness(int8_t *i1, int8_t *i2);// , stringstream *ss);
													// get a closeness betreen two rgb thumbnails
	static string tolower(string s) { transform(s.begin(), s.end(), s.begin(), ::tolower); return s; }
};
