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


#include "cimage.h"


void usage()
{
	logger::raw("usage: cimage [-c <ic> <files>|-s <ic> <search>] [-nt] [-cm <cmethod>]");
	logger::raw("where:");
	logger::raw("-c       : create database (with threads)");
	logger::raw("-s       : search database (with list threading)");
	logger::raw("-nt      : don't use threading");
	logger::raw("<ic>     : image colletion location");
	logger::raw("<files>  : images to add");
	logger::raw("<search> : image to search for");
	logger::raw("<cmethod> : image search type: 's' simple, 'm' mono, 'l' luma, 'a' assembler");
	exit(0);
}

// Get files path & check it exists
fs::path checkFiles(string spath)
{
	
	fs::path files(spath);
	files = fs::canonical(files);
	if (!fs::exists(files))
	{
		logger::fatal("Directory " + files.string() + " does not exist");
	}
	return files;
}

fs::path checkSet(string spath, bool createIfNeeded)
{
	// Get set path & create directory id needed
	fs::path set(spath);
	if (fs::exists(set))
	{
		if (!fs::is_directory(set))
		{
			logger::fatal("Path " + set.string() + " exists and isnt a directory");
		}
	}
	else if ( createIfNeeded )
	{
		logger::info("Creating Path " + set.string());
		fs::create_directory(set);
		logger::info("Created Path " + set.string());
	}
	else
		logger::fatal("Path " + set.string() + " dosn't exist and isnt set to be created.");

	return set;

}

int main(int argc, char *argv[])
{

#if defined(LINUX)
	cout << "Linux version" << endl;
#elif defined(_WIN64) 
	cout << "WIN64 version" << endl;
#elif defined(_WIN32)
	cout << "WIN32 version" << endl;
#endif
#if defined(_DEBUG)
	cout << "DEBUG version" << endl;
#endif

	// Start logging. To screen and logfile
	logger::to("cimage.log", logger::Debug, logger::Info);

	// Check we have enough arguments to start
	if ( argc < 4 )
		usage();

	// this is what we are going to do 
	string action = argv[1];
	string sset = argv[2];
	string sfiles = argv[3];

	// Initialize the free image library
	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// any additional switches
	bool useThreads = true;
	ImgUtils::SearchType istype = ImgUtils::SearchType::Simple;
	for(int ax = 0; ax < argc; ax++)
	{
		string par = string(argv[ax]);
		string par2 = "";
		if ( ax < argc - 1)
			par2 = string(argv[ax+1]);

		if (par == "-nt")
			useThreads = false;
		if (par == "-cm" && par2.length() > 0)
		{
			switch (par2[0])
			{
			case 's': istype = ImgUtils::SearchType::Simple; break;
			case 'm': istype = ImgUtils::SearchType::Mono; break;
			case 'l': istype = ImgUtils::SearchType::Luma; break;
			case 'a': istype = ImgUtils::SearchType::Assembler; break;
			default: usage(); break;
			}
		}
	}

	// This is create...
	if (action  == "-c")
	{
		ImgCollectionBuilder::CreateType ctype = useThreads ? ImgCollectionBuilder::CreateType::CREATETHREADS : ImgCollectionBuilder::CreateType::CREATENOTHREADS;

		logger::info("Create ImgCollection");

		// Get files & db path & check it exists
		fs::path files = checkFiles(sfiles);
		fs::path set = checkSet(sset, true);
		if (!fs::exists(files))
			logger::fatal("Dir to scan does not exist");

		// create a Set builder. This will form the ImgCollection in memory
		ImgCollectionBuilder *sb = new ImgCollectionBuilder(ctype);

		// Add all files to ther ImgCollection
		Timer::start();
		sb->Create(files);
		Timer::stop("Scaning");

		// Save the set to db
		Timer::start();
		sb->Save(set);
		Timer::stop("Saving ");

	}
	// this is a search...
	else if (action == "-s")
	{
		ImgCollectionSearch::SrchType stype = useThreads ? ImgCollectionSearch::SrchType::SRCHLIST : ImgCollectionSearch::SRCHNOTHRD;

		// Get files path & check it exists
		fs::path set = checkSet(sset, true);
		fs::path search(sfiles);
		if (!fs::exists(search))
			logger::fatal("File to search does not exist");

		// load the ImgCollection from disk into a ImgCollectionBuilder
		if (!fs::exists(set))
			logger::fatal("File to search does not exist");
		ImgCollectionSearch *sb = new ImgCollectionSearch(stype, istype);
		Timer::start();
		sb->Load(set);
		Timer::stop("Loaded set " + set.string());

		// search for the image
		Timer::start();
		sb->Find(search);
		Timer::stop("Scan complete");
	}
	else
		usage();

	// print some stats
	logger::info(Timer::report());
	FreeImage_DeInitialise();


    return 0;


}

