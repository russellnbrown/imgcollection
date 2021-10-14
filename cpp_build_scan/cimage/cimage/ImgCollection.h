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
// ImgCollection
//
// These 3 structures form the ImgCollection. 
// These are saved/loaded to a set of files on disk
//

class ImgCollection
{
public:


	list<ImgCollectionDirItem*> dirs;				// simple lists for DirItem & FileItem
	list<ImgCollectionFileItem*> files;
	map<int64_t, ImgCollectionImageItem*> images;	// hash table for ImageItem to easilly
													// lookup & check duplicates etc
	fs::path top;									// the top level directory. all set directories relative to this
	string stop;									// string of above with '/' as file seperator

	bool Save(fs::path dir);						// save the ImgCollection to file set


};