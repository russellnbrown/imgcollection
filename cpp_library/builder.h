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
struct st_mysql;
struct st_mysql_stmt;

//
// builder
//
// Builder brings together everything needed to create a collection
// from a filesystem. It includes the file system scanner, build threads to 
// create thumbnails etc.
//
class icBuilder
{

public:
	icBuilder();
	~icBuilder();

public:

	CollUPtr Create(fs::path path);			// create the ImgCollection 

	// load the ImgCollection from database
	bool Save(CollUPtr& coll, fs::path dir);// save the collection to files
#ifdef USEDB 
	bool Save(CollUPtr& coll, string host, int port, string db, string user, string passwd); // save the collection to database
#endif
	static bool Test(string host, int port, string db, string user, string passwd); // test database connection

private:

	CollUPtr ic;											// The ImgCollection

	bool running;											// used to stop image processing threads
	icStats st;												// stats on processing
	list<shared_ptr<icBuildThreadInfo>> threads;				// image processing threads
	list<shared_ptr<ImageInfo>> threadFeeder;				// queue for feeding files to image processing threads
	mutex tflock;											// lock for above
	string stop;											// string of above with '/' as file seperator
	mutex llock;											// lock for changing content of set lists
	int numThreads;											// number of threads we can use for threaded operations

private:
	bool walkFiles(fs::path dir);							// iterates over files in a directory tree 
	HKey pathsplit(const fs::path d, string& dir,
		string& file);										// split a file path into its dir & file (if any) componenets

	void processItem(shared_ptr<ImageInfo>&ii);				// process an image file
	void processItemResult(shared_ptr<ImageInfo>&ii);		// process result of above
	void waitOnBuildThreads();								// wait for all image processing threads to stop
	void buildThread(shared_ptr<icBuildThreadInfo>ri);		// image processing thread method
#ifdef USEDB
	bool insert_dir(struct st_mysql* con, HKey dhash, const char* name);
	bool insert_file(struct st_mysql* con, HKey dhash, HKey crc, const char* name);
	bool insert_thumb(struct st_mysql* con, HKey key, int8_t *thumb);
	void show_stmt_error(struct st_mysql_stmt* stmt);
	void finish_with_error(struct st_mysql* con);
#endif
};

