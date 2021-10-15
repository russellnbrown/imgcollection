
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

	Stats st;								// stats on processing
	ImageInfo* searchItem;					// in searches, the image being searched for
	fs::path saveTo;

public:
	ImgCollectionRefresh();

public:

	void Create(fs::path to);
	void Load(fs::path from);
	void Save();
	void Refresh(fs::path scan);

private:
	bool walkDirecrories(fs::path dir);			// iterates over files in a directory tree 
	uint32_t pathsplit(const fs::path d, string& dir, string& file, time_t& lastMod);
	// split a file path into its dir & file (if any) componenets

	map<uint32_t, ImgCollectionDirItem*> dirs;
	map<uint64_t, ImgCollectionFileItem*> files;


	void processItem(ImageInfo* ii);		// process an image file
	void processItemResult(ImageInfo* ii);	// process result of above

	bool isSubdir(fs::path full, fs::path sub);
	uint64_t dfHash(uint32_t dhash, string filename);
};

