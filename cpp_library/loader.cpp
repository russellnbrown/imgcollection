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

#include "cpp_library.h"
#include "cpp_library_internal.h"
#ifdef USEDB
#include <mysql.h>
#endif

 // Builder. This is a helper class to build an Image Collection.
 // It can use a simple or multithreaded approach.

//
// constructor.
//
// We will create the encoding threads. They will wait for tasks to
// put onto their input queue of for an exit to be signalled.
//
icLoader::icLoader()
{
	numThreads = std::thread::hardware_concurrency() * 2;
	if (numThreads <= 0)
		numThreads = 2;

}

icLoader::~icLoader()
{
	//logger::info("loader object deleted");
}



// Two ways to use the loader, either Create ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
unique_ptr<icCollection> icLoader::Load(fs::path path)
{
	ic = make_unique<icCollection>();

	setToLoad = path;
	logger::info("Loading with threads");
	// We can split loading into 3 threads, one to read each
	thread dt = thread(&icLoader::loadDirs, this);
	thread ft = thread(&icLoader::loadFiles, this);
	thread it = thread(&icLoader::loadImages, this);
	dt.join();
	ft.join();
	it.join();

	logger::info("Load from flat files, dirs=" + to_string(ic->dirs.size()) + ", files=" + 
		          to_string(ic->files.size()) + ", images=" + to_string(ic->imageMap.size()) + ", ivec=" + ", images=" + to_string(ic->images.size()));
	return std::move(ic);

}



void icLoader::loadDirs()
{
	string line;
	ifstream idir(setToLoad.string() + "/dirs.txt");
	getline(idir, line);

	ic->top = fs::path(line);

	if (!fs::exists(ic->top))
		logger::fatal("Top dir " + line + " does not exist.");


	while (getline(idir, line))
	{
		unique_ptr<CollectionDirItem>d = CollectionDirItem::fromSave(line);
		if (d)
		{
			ic->dirs.push_back(std::move(d));
		}
	}
	idir.close();

	return;
}

void icLoader::loadFiles()
{
	string line;

	ifstream ifile(setToLoad.string() + "/files.txt");
	while (getline(ifile, line))
	{
		unique_ptr<CollectionFileItem> f = CollectionFileItem::fromSave(line);
		if (f)
		{
			ic->files.push_back(std::move(f));
		}
	}
	ifile.close();


	return;
}

void icLoader::loadImages()
{
	ifstream iimg(setToLoad.string() + "/images.bin", ios::binary);
	int32_t icrc;
	int8_t* thumb;
	int stx = 0;
	thumb = new int8_t[TNMEM];
	ic->images.reserve(ic->files.size());//reserve space for performance, images will be <= num files
	while (true)
	{

		if (iimg.read((char*)&icrc, sizeof(icrc)))
		{
			iimg.read((char*)thumb, TNMEM);
			shared_ptr<CollectionImageItem> img = make_shared<CollectionImageItem>(icrc, thumb);
			ic->images.push_back(img);
			ic->imageMap[icrc] = img;
			// images are stored in a vector for quick / easy thread iteration in searches and
			// also in a map for easy lookup / duplicate detection
		}
		else
			break;
	}
	iimg.close();
	ic->images.shrink_to_fit();// release anything we reserved but didnt need

	return;
}

#ifdef USEDB

unique_ptr<icCollection> icLoader::Load(string host, int port, string db, string user, string passwd)
{
	ic = make_unique<icCollection>();

	MYSQL* con = mysql_init(NULL);
	if (con == NULL)
		finish_with_error(con);

	if (mysql_real_connect(con, host.c_str(), user.c_str(), passwd.c_str(), db.c_str(), port, NULL, 0) == NULL)
		finish_with_error(con);

	MYSQL_RES* res;
	MYSQL_ROW row;

	// Dirs....

	string stop = "SELECT path FROM top";
	if (mysql_query(con, stop.c_str()))
		finish_with_error(con);
	res = mysql_store_result(con);
	int num_fields = mysql_num_fields(res); // going to be 1
	if (num_fields != 1)
		logger::fatal("Error reading top from DB ( col count wrong )");
	while ((row = mysql_fetch_row(res)))
	{
		if (row[0] != NULL)
		{
			ic->top = fs::path(row[0]);
			if (!fs::exists(ic->top))
				logger::fatal("Top dir " + ic->top.string() + " does not exist.");
		}
	}
	if (res != NULL)
		mysql_free_result(res);

	stop = "SELECT dkey,path FROM dirs";
	if (mysql_query(con, stop.c_str()))
		finish_with_error(con);
	res = mysql_store_result(con);
	num_fields = mysql_num_fields(res); // going to be 2
	if (num_fields != 2)
		logger::fatal("Error reading directories from DB ( col count wrong )");
	while ((row = mysql_fetch_row(res)))
	{
		if (row[0] == NULL || row[1] == NULL)
			logger::fatal("Missing column in dirs");
		string path = row[1];
		int32_t dkey = atol(row[0]);
		unique_ptr<CollectionDirItem>d = make_unique<CollectionDirItem>(dkey, path);
		if (d)
		{
			ic->dirs.push_back(std::move(d));
		}
	}
	if (res != NULL)
		mysql_free_result(res);


	stop = "SELECT dkey,ikey,name FROM files";
	if (mysql_query(con, stop.c_str()))
		finish_with_error(con);
	res = mysql_store_result(con);
	num_fields = mysql_num_fields(res); // going to be 3
	if (num_fields != 3)
		logger::fatal("Error reading files from DB ( col count wrong )");
	while ((row = mysql_fetch_row(res)))
	{
		if (row[0] == NULL || row[1] == NULL || row[2] == NULL )
			logger::fatal("Missing column data in files");
		string name = row[2];
		int32_t dkey = atol(row[0]);
		int32_t ikey = atol(row[1]);
		unique_ptr<CollectionFileItem>f = make_unique<CollectionFileItem>(dkey, ikey, name);
		if (f)
		{
			ic->files.push_back(std::move(f));
		}
	}
	if (res != NULL)
		mysql_free_result(res);

	ic->images.reserve(ic->files.size());
	stop = "SELECT ikey,thumb FROM images";
	if (mysql_query(con, stop.c_str()))
		finish_with_error(con);
	res = mysql_store_result(con);
	num_fields = mysql_num_fields(res); // going to be 2
	if (num_fields != 2)
		logger::fatal("Error reading images from DB ( col count wrong )");
	while ((row = mysql_fetch_row(res)))
	{
		if (row[0] == NULL || row[1] == NULL  )
			logger::fatal("Missing column data in files");
		int32_t icrc = atol(row[0]);
		shared_ptr<CollectionImageItem> img = make_shared<CollectionImageItem>(icrc, (int8_t*)row[1]);
		ic->images.push_back(img);
		ic->imageMap[icrc] = img;
	}
	if (res != NULL)
		mysql_free_result(res);
	ic->images.shrink_to_fit();

	mysql_close(con);

	logger::info("Load from database, dirs=" + to_string(ic->dirs.size()) + ", files=" +
		to_string(ic->files.size()) + ", images=" + to_string(ic->imageMap.size()) + ", ivec=" + ", images=" + to_string(ic->images.size()));

	return std::move(ic);
}

//
// finish_with_error
//
// if something goes wrong with a DB operation, print out the error & exit
//
void icLoader::finish_with_error(MYSQL* con)
{
	fprintf(stderr, "%s\n", mysql_error(con));
	mysql_close(con);
	exit(1);
}

#endif