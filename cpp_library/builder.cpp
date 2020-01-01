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
icBuilder::icBuilder()
{

	// Build is multithreaded, find out how many to based on the number of
	// processors/cores in the machine. Using 2 * this number seems to be 
	// quickest

	numThreads = std::thread::hardware_concurrency() * 2;
	if (numThreads <= 0)
		numThreads = 2;

	// flag used to end threads when needed
	running = true;

	// start each encoder if configured
	for (int t = 0; t < numThreads; t++)
	{
		shared_ptr<icBuildThreadInfo> rti = make_shared<icBuildThreadInfo>();
		rti->id = t;
		rti->trd = thread(&icBuilder::buildThread, this, rti);
		threads.push_back(rti);
		st.incThreads();
	}
}

icBuilder::~icBuilder()
{
	//logger::info("builder object deleted");
}



// The 'image processing' thread
void icBuilder::buildThread(shared_ptr<icBuildThreadInfo>ri)
{
	logger::info("Thread " + to_string(ri->id) + " running");
	ImageInfoSPtr current;
	int tp = 0;

	//
	// loop round reading items to process off the threadFeeder queue (populated by the fileWalker )
	//
	while (true)
	{
		// get next job
		tflock.lock();
		int ts = threadFeeder.size();
		if (ts > 0)
		{
			current = threadFeeder.front();
			threadFeeder.pop_front();
		}
		tflock.unlock();
		// nothing to do, have a nap and try again
		if (current == nullptr)
		{
			if (!running)
				break;
			MSNOOZE(10);
			continue;
		}

		tp++;
		//logger::debug("Processing " + current->filepart + " in thread " + to_string(ri->id));
		try
		{
			if (icUtils::GetImageInfo(current))
				processItemResult(current);
			else
				st.incErrors();
		}
		catch (std::runtime_error & e)
		{
			logger::error("Error processing " + current->filepart + ", " + string(e.what()));
		}
		catch (std::exception & e)
		{
			logger::error("Error processing " + current->filepart + ", " + string(e.what()));
		}
		current = nullptr;
	}
	logger::info("Thread " + to_string(ri->id) + " stopping, it processed " + to_string(tp) + " images.");
}


// Two ways to use the builder, either Create ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
icCollection *icBuilder::Create(fs::path path)
{
	ic = new icCollection();

	ic->top = path;
	stop = ic->top.string();
	icUtils::Replace(stop, "\\", "/");

	// call file walker with the 'top' directory
	walkFiles(path);

	return ic;

}

HKey icBuilder::pathsplit(const fs::path d, string& dirpart, string& filepart)
{
	try
	{
		if (fs::is_regular_file(d))
		{
			filepart = d.filename().string();
			dirpart = d.parent_path().string();
		}
		else if (fs::is_directory(d))
		{
			filepart = "";
			dirpart = d.string();
		}
		else
			return 0;
	}
	catch (std::exception e)
	{
		logger::warn("Ignore ");
		return 0;
	}

	icUtils::Replace(dirpart, "\\", "/");
	dirpart = dirpart.substr(ic->top.string().length());

	if (dirpart.empty() || dirpart[0] != '/')
		dirpart = "/" + dirpart;

	return icUtils::GetHash(dirpart);
}

//
// walkFiles
//
// The main 'Create' method. Called initially from the 'top' directory
// we then loop through files in that dir, if we find a directory, call ourselves
// recursivly. If we find a file, call processFile to deal with it ( in 
// imgProcessingThread )
//
bool icBuilder::walkFiles(fs::path dir)
{

	string dirpart;
	string filepart;

	// add 'top' directory to the collection 
	HKey dirHash = pathsplit(dir, dirpart, filepart);
	ic->dirs.push_back(make_unique<CollectionDirItem>(dirHash, dirpart));
	st.incDirs();

	// loop for all its files/subdirs
	int fcntr = 0;
	for (fs::directory_entry de : fs::recursive_directory_iterator(dir))
	{
		string dirpart;
		string filepart;

		// get crc32 of the directory path
		HKey dirHash = pathsplit(de, dirpart, filepart);
		if (dirHash == 0)
		{
			st.incNameErrors();
			continue;
		}

		// givee occasional feedback
		if (++fcntr % 5000 == 0)
		{
			logger::info("At: " + st.progressStr());
		}

		// if its a directory, create a DirItem and add to collection
		if (filepart.empty())
		{
			st.incDirs();
			ic->dirs.push_back(make_unique<CollectionDirItem>(dirHash, dirpart));
			//logger::debug("Dir is " + dirpart);
		}
		else
		{
			// if its an image file, create a ImageInfo and process it
			st.incFiles();
			if (icUtils::IsImageFile(de.path()))
			{
				shared_ptr<ImageInfo> ii = make_shared<ImageInfo>();
				ii->de = de;
				ii->dirhash = dirHash;
				ii->filepart = filepart;
				processItem(ii);
			}
		}
	}

	// wait for image processing threads to stop
	waitOnBuildThreads();

	return true;
}

//
// waitOnProcessingThreads
// 
// called when the directory walker has finished finding files, we wait
// for the encoding threads to finish their job
//
void icBuilder::waitOnBuildThreads()
{
	logger::info("Running set to false");

	// signal threads we are finished
	running = false;

	// and wait for them to exit
	for (auto &&rt = threads.begin(); rt != threads.end(); rt++)
	{
		rt->get()->trd.join();
		logger::debug("Finished " + to_string(rt->get()->id) + " has finished normally.");
	}

}

void icBuilder::processItemResult(shared_ptr<ImageInfo>&ii)
{
	//logger::info("Results " + ii->filepart);
	string err;

	st.addBytes(ii->size);
	{
		scoped_lock sl(llock);
		try
		{
			ic->files.push_back(make_unique<CollectionFileItem>(ii->dirhash, ii->crc, ii->filepart));
			if (ic->imageMap.find(ii->crc) == ic->imageMap.end())
			{
				shared_ptr<CollectionImageItem> img = make_shared<CollectionImageItem>(ii->crc, ii->thumb);
				ic->imageMap[ii->crc] = img;
				st.incImages();
			}
			else
				st.incDuplicates();
		}
		catch (exception & e)
		{
			st.incErrors();
			err = "Could not gen image " + ii->filepart + string(e.what());
		}
	}

	//delete ii;


	if (err.length() > 0)
		logger::error(err);

}

void icBuilder::processItem(shared_ptr<ImageInfo>&ii)
{
	// process an image file

	try
	{
		// Add the ImageInfo to the thread feeder queue, 
		// a thread will pick it off when it is ready
		// if too many encodes are already in Q just wait
		while (threadFeeder.size() > (threads.size() * 10))
			MSNOOZE(10);

		// add to queue (protected as threads will access and remove items )
		tflock.lock();
		threadFeeder.push_back(ii);
		tflock.unlock();
	}
	catch (runtime_error & e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}
	catch (exception & e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}

}


//
// Save
//
// This saves the Collection as three seperate flat files, dirss, files & images
//
bool icBuilder::Save(icCollection *coll, fs::path dir)
{
	string dirpart;
	string filepart;

	string tstr = coll->top.string();
	icUtils::Replace(tstr, "\\", "/");

	ofstream odir(dir.string() + "/dirs.txt");
	odir << tstr << endl;
	for (auto&& it = coll->dirs.begin(); it != coll->dirs.end(); ++it)
	{
		odir << it->get()->toSave() << endl;
	}
	odir.close();

	ofstream ofile(dir.string() + "/files.txt");
	for (auto&& it = coll->files.begin(); it != coll->files.end(); ++it)
	{
		ofile << it->get()->toSave() << endl;
	}
	ofile.close();

	ofstream oimg(dir.string() + "/images.bin", ios::out | ios::binary);
	for (auto&& it = coll->imageMap.begin(); it != coll->imageMap.end(); ++it)
	{
		int64_t icrc = static_cast<int64_t>(it->first);
		oimg.write((char*)&icrc, sizeof(icrc));
		oimg.write((char*)it->second.get()->thumb.data(), TNMEM);
	}
	oimg.close();


	return true;
}

#ifdef USEDB

//
// insert_thumb
//
// Use binding to speed up database loading
//
bool icBuilder::insert_thumb(MYSQL* con, HKey key, int8_t* thumb)
{

	const int chunkSize = 500;
	static int storeAt = 0;
	static char* thumbs[chunkSize];
	static long long int keys[chunkSize];
	static unsigned long thumbLens[chunkSize];

	if (thumb && storeAt < chunkSize)
	{
		thumbs[storeAt] = (char*)thumb;
		thumbLens[storeAt] = TNMEM;
		keys[storeAt] = key;
		storeAt++;
	}
	if (storeAt < chunkSize - 1 && thumb)
		return false;

	logger::info("Saving " + to_string(storeAt) + " images.");

	MYSQL_BIND bind[4];
	memset(bind, 0, sizeof(MYSQL_BIND) * 4);
	bind[0].buffer_type = MYSQL_TYPE_LONGLONG;
	bind[0].buffer = &keys[0];
	bind[0].length = 0;
	bind[0].is_null = 0;

	unsigned long sse = TNMEM;
	bind[1].buffer_type = MYSQL_TYPE_BLOB;
	bind[1].buffer = thumbs;
	bind[1].length = thumbLens;
	bind[1].is_null = 0;

	MYSQL_STMT* stmt;
	stmt = mysql_stmt_init(con);
	if (mysql_stmt_prepare(stmt, "INSERT INTO images (ikey, thumb) VALUES (?,?)", -1))
		show_stmt_error(stmt);

	int array_size = storeAt;
	mysql_stmt_attr_set(stmt, STMT_ATTR_ARRAY_SIZE, &array_size);
	mysql_stmt_bind_param(stmt, bind);


	try
	{
		if (mysql_stmt_execute(stmt))
			show_stmt_error(stmt);
	}
	catch (std::exception e)
	{
		printf("%s\n", e.what());
	}
	mysql_stmt_free_result(stmt);
	mysql_stmt_close(stmt);

	storeAt = 0;

	return true;

}


//
// insert_file
//
// Use binding to speed up database loading
//
bool icBuilder::insert_file(MYSQL* con, HKey dhash, HKey crc, const char* name)
{
	const int chunkSize = 500;
	static int storeAt = 0;
	static const char* names[chunkSize];
	static long long int dhashs[chunkSize];
	static long long int crcs[chunkSize];
	static unsigned long nameLens[chunkSize];

	if (name && storeAt < chunkSize)
	{
		names[storeAt] = name;
		nameLens[storeAt] = strlen(name);
		crcs[storeAt] = crc;
		dhashs[storeAt] = dhash;
		storeAt++;
	}
	if ((storeAt < chunkSize - 1) && (name != 0))
		return false;

	logger::info("Saving " + to_string(storeAt) + " files.");

	MYSQL_BIND bind[4];
	memset(bind, 0, sizeof(MYSQL_BIND) * 4);
	bind[0].buffer_type = MYSQL_TYPE_LONGLONG;
	bind[0].buffer = &dhashs[0];
	bind[0].length = 0;
	bind[0].is_null = 0;

	bind[1].buffer_type = MYSQL_TYPE_LONGLONG;
	bind[1].buffer = &crcs[0];
	bind[1].length = 0;
	bind[1].is_null = 0;

	bind[2].buffer_type = MYSQL_TYPE_STRING;
	bind[2].buffer = names;
	bind[2].length = nameLens;
	bind[2].is_null = 0;

	MYSQL_STMT* stmt;
	stmt = mysql_stmt_init(con);
	if (mysql_stmt_prepare(stmt, "INSERT INTO files(dkey,ikey,name)  VALUES (?,?,?)", -1))
		show_stmt_error(stmt);

	int array_size = storeAt;
	mysql_stmt_attr_set(stmt, STMT_ATTR_ARRAY_SIZE, &array_size);
	mysql_stmt_bind_param(stmt, bind);


	try
	{
		if (mysql_stmt_execute(stmt))
			show_stmt_error(stmt);
	}
	catch (std::exception e)
	{
		printf("%s\n", e.what());
	}
	mysql_stmt_free_result(stmt);
	mysql_stmt_close(stmt);

	storeAt = 0;

	return true;

}

//
// insert_dir
//
// Use binding to speed up database loading
//
bool icBuilder::insert_dir(MYSQL* con, HKey dhash, const char* name)
{
	const int chunkSize = 500;
	static int storeAt = 0;
	static const char* names[chunkSize];
	static long long int dhashs[chunkSize];
	static unsigned long nameLens[chunkSize];

	if (name && storeAt < chunkSize)
	{
		names[storeAt] = name;
		nameLens[storeAt] = strlen(name);
		dhashs[storeAt] = dhash;
		storeAt++;
	}
	if ((storeAt < chunkSize - 1) && (name != 0))
		return false;

	logger::info("Saving " + to_string(storeAt) + " dirs.");

	MYSQL_BIND bind[4];
	memset(bind, 0, sizeof(MYSQL_BIND) * 4);
	bind[0].buffer_type = MYSQL_TYPE_LONGLONG;
	bind[0].buffer = &dhashs[0];
	bind[0].length = 0;
	bind[0].is_null = 0;

	bind[1].buffer_type = MYSQL_TYPE_STRING;
	bind[1].buffer = names;
	bind[1].length = nameLens;
	bind[1].is_null = 0;

	MYSQL_STMT* stmt;
	stmt = mysql_stmt_init(con);
	if (mysql_stmt_prepare(stmt, "INSERT INTO dirs(dkey, path)  VALUES (?,?)", -1))
		show_stmt_error(stmt);

	int array_size = storeAt;
	mysql_stmt_attr_set(stmt, STMT_ATTR_ARRAY_SIZE, &array_size);
	mysql_stmt_bind_param(stmt, bind);


	try
	{
		if (mysql_stmt_execute(stmt))
			show_stmt_error(stmt);
	}
	catch (std::exception e)
	{
		printf("%s\n", e.what());
	}
	mysql_stmt_free_result(stmt);
	mysql_stmt_close(stmt);

	storeAt = 0;

	return true;

}


//
// finish_with_error
//
// if something goes wrong with a DB operation, print out the error & exit
//
void icBuilder::finish_with_error(MYSQL* con)
{
	fprintf(stderr, "%s\n", mysql_error(con));
	mysql_close(con);
	exit(1);
}

//
// show_stmt_error
//
// if SQL error occurs, print out the error & exit
//
void icBuilder::show_stmt_error(MYSQL_STMT* stmt)
{
	printf("Error(%d) [%s] \"%s\"", mysql_stmt_errno(stmt),
		mysql_stmt_sqlstate(stmt),
		mysql_stmt_error(stmt));
	exit(-1);
}



//
// Test
//
// This checks database credentials work
//
bool icBuilder::Test(string host, int port, string db, string user, string passwd)
{
	bool ok = false;

	MYSQL* con = mysql_init(NULL);
	if (con == NULL)
		return ok;

	if (mysql_real_connect(con, host.c_str(), user.c_str(), passwd.c_str(), db.c_str(), port, NULL, 0) == NULL)
	{
		ok = false;
		logger::error("DB Connect error: " + string(mysql_error(con)));
	}
	else
		ok = true;

	mysql_close(con);
	return ok;

}

//
// Save
//
// This saves the ImgCollection to a mysql database
//
bool icBuilder::Save(icCollection *coll, string host, int port, string db, string user, string passwd)
{
	string dirpart;
	string filepart;

	string tstr = coll->top.string();
	icUtils::Replace(tstr, "\\", "/");

	MYSQL* con = mysql_init(NULL);
	if (con == NULL)
		finish_with_error(con);

	if (mysql_real_connect(con, host.c_str(), user.c_str(), passwd.c_str(), db.c_str(), port, NULL, 0) == NULL)
		finish_with_error(con);

	if (mysql_query(con, "use imgcollection"))
		finish_with_error(con);
	if (mysql_query(con, "truncate table top"))
		finish_with_error(con);
	if (mysql_query(con, "truncate table dirs"))
		finish_with_error(con);
	if (mysql_query(con, "truncate table files"))
		finish_with_error(con);
	if (mysql_query(con, "truncate table images"))
		finish_with_error(con);
	if (mysql_query(con, "truncate table top"))
		finish_with_error(con);
	string stop = "INSERT INTO top VALUES('" + coll->top.string() + "')";
	if (mysql_query(con, stop.c_str()))
		finish_with_error(con);


	for (auto&& it = coll->dirs.begin(); it != coll->dirs.end(); ++it)
	{
		insert_dir(con, (*it)->hash, (*it)->path.c_str());
	}
	insert_dir(con, 0, 0);


	for (auto&& it = coll->files.begin(); it != coll->files.end(); ++it)
	{
		insert_file(con, (*it)->dhash, (*it)->crc, (*it)->name.c_str());
	}
	insert_file(con, 0, 0, 0);


	for (auto&& it = coll->imageMap.begin(); it != coll->imageMap.end(); ++it)
	{
		shared_ptr<CollectionImageItem> &ci = it->second;
		CollectionImageItem* cip = ci.get();
		int8_t* td = cip->thumb.data();

		insert_thumb(con, it->second->crc, td );
	}
	insert_thumb(con, 0, 0);

	mysql_close(con);
	return true;
}

#endif