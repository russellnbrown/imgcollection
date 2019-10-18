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
	logger::raw("usage: cimage [-c <ic> <files>|-s <ic> <search>]");
	logger::raw("where:");
	logger::raw("-c       : create database");
	logger::raw("-s       : search database");
	logger::raw("<ic>     : image colletion location");
	logger::raw("<files>  : images to add");
	logger::raw("<search> : image to search for");
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

	// Start logging. To screen and logfile
	logger::to("cimage.log", logger::Debug, logger::Info);

	// Check we have enough arguments to start
	if ( argc < 3 )
		usage();

	// this is what we are going to do 
	string action = argv[1];

	// Initialize the free image library
	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));


	// This is create...
	if (argc == 4 && action == "-c")
	{
		logger::info("Create ImgCollection");



		// Get files & db path & check it exists
		fs::path files = checkFiles(argv[3]);
		fs::path set = checkSet(argv[2], true);
		if (!fs::exists(files))
			logger::fatal("Dir to scan does not exist");

		// create a Set builder. This will form the ImgCollection in memory
		ImgCollectionBuilder *sb = new ImgCollectionBuilder();

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
	else if (argc == 4 && action == "-s")
	{
		// Get files path & check it exists
		fs::path set = checkSet(argv[2], true);
		fs::path search(argv[3]);
		if (!fs::exists(search))
			logger::fatal("File to search does not exist");

		// load the ImgCollection from disk into a ImgCollectionBuilder
		if (!fs::exists(set))
			logger::fatal("File to search does not exist");
		ImgCollectionSearch *sb = new ImgCollectionSearch();
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

