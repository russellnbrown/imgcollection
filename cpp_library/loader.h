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

 // forwards so we dont need to include
 // whole mysql includes everywhere
#ifdef USEDB
struct st_mysql;
struct st_mysql_stmt;
#endif

//
// loader
//
// Builder brings together everything needed to create a collection
// from a filesystem. It includes the file system scanner, build threads to 
// create thumbnails etc.
//
class icLoader
{

public:
	icLoader();
	~icLoader();

public:

	icCollection *Load(fs::path path);				// load the ImgCollection from flat files
#ifdef USEDB
	icCollection *Load(string host, int port, string db, string user, string passwd);
#endif

private:

	icCollection *ic;										// The ImgCollection

	mutex llock;											// lock for changing content of set lists
	list<shared_ptr<SearchResult>> results;					// in searches, the results of comparisons
	ImageInfo* searchItem;									// in searches, the image being searched for
	int numThreads;											// number of threads we can use for threaded operations

private:

	void loadImages();										// Load images from file set
	void loadFiles();										// Load images from database set
	void loadDirs();										// Load directories from database set
#ifdef USEDB
	void finish_with_error(struct st_mysql* con);
#endif
	fs::path setToLoad;

};

