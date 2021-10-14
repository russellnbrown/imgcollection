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

//
// usage 
//
// called when someting wrong with command line arguments, missing files etc.
// -sn set.db file_5.png asm z
// -c set.db C:\TestEnvironments\img
//
void usage()
{
	logger::raw("usage: cimage [-c <top> <ic> <files>|-a <ic> <files>|-s <ic> <search>|-h|-m <nc out> <ic 1>..<ic n>|-r <icin> <icout> <files>] [-nt] [-cm <cmethod>]");
	logger::raw("where:");
	logger::raw("-c        : create database (with threads)");
	logger::raw("-m        : merge databases");
	logger::raw("-r        : refresh set");
	logger::raw("-s        : search database (with list threading)");
	logger::raw("-nt       : don't use threading");
	logger::raw("<top>     : top location");
	logger::raw("<ic>      : image colletion location");
	logger::raw("<files>   : images to add");
	logger::raw("<search>  : image to search for");
	logger::raw("<cmethod> : image search type: 's' simple, 'm' mono, 'l' luma, 'a' assembler");
	logger::raw("-h        : display help");
	exit(0); // give up !
}

//
// checkFiles
//
// Check if a file exists and return a path
//
fs::path checkFiles(string spath)
{	
	fs::path files(spath);
	
	if (!fs::exists(files))
	{
		logger::fatal("File " + files.string() + " does not exist");
	}
	files = fs::canonical(files);
	return files;
}

//
// checkSet
//
// Check if a set's directory exists, optionally create it if flag is set
//
fs::path checkSet(string spath, bool createIfNeeded)
{
	// check if it exists and is a dirctory
	fs::path set(spath);
	if (fs::exists(set))
	{
		if (!fs::is_directory(set))
			logger::fatal("Path " + set.string() + " exists and isnt a directory");
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
	cout << "WIN64 version" << endl;  // elif because WIN64 also defines WIN32
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

	// help
	if (action == "-h")
		usage();

	// Initialize the free image library
	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// any additional switches
	bool useThreads = true;
	ImgUtils::SearchType istype = ImgUtils::SearchType::Simple;
	for(int ax = 0; ax < argc; ax++)
	{
		// par is current arg & par2 is next if there
		string par = string(argv[ax]);
		string par2 = "";
		if ( ax < argc - 1)
			par2 = string(argv[ax+1]);

		// -nt - no threads
		if (par == "-nt")
			useThreads = false;
		// -cm closeness mode
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

		// Get files & db path & check it exists - exits of not
		fs::path files = checkFiles(sfiles);
		fs::path set = checkSet(sset, true);
		fs::path top = argv[4];


		// create a builder. This will form the ImgCollection in memory
		ImgCollectionBuilder *sb = new ImgCollectionBuilder(ctype);

		// Add all files to ther ImgCollection
		Timer::start();
		sb->Create(top,files, set);
		Timer::stop("Scaning");

		// Save the set to db
		Timer::start();
		sb->Save();
		Timer::stop("Saving ");

	}
	else if (action == "-m")
	{

		
		if (fs::exists(sset+"/dirs.txt"))
			logger::fatal("Output set exists");
		fs::path outputSet = checkSet(sset, true);
		logger::info("Output set is " + outputSet.string() );
		ImgCollectionMerge* sb = new ImgCollectionMerge(outputSet);

		for (int msx = 3; msx < argc; msx++)
		{
			fs::path inputSet = checkSet(argv[msx], false);
			logger::info("Input set is " + inputSet.string());
			sb->Append(inputSet);
		}

		sb->Save();

	}
	// this is a search...
	else if (action == "-s")
	{
		ImgCollectionSearch::SrchType stype = useThreads ? ImgCollectionSearch::SrchType::SRCHLIST : ImgCollectionSearch::SRCHNOTHRD;

		// Get files path & check it exists
		fs::path set = checkSet(sset, false);
		fs::path search(sfiles);
		if (!fs::exists(search))
			logger::fatal("File to search does not exist");

		// load the ImgCollection from disk into a ImgCollectionBuilder
		ImgCollectionSearch *sb = new ImgCollectionSearch(stype, istype);
		Timer::start();
		sb->Load(set);
		Timer::stop("Loaded set " + set.string());

		// search for the image
		Timer::start();
		sb->Find(search);
		Timer::stop("Scan complete");
	}
	else if (action == "-r")
	{
		if (argc != 5)
			usage();
		string refreshInSet = argv[2];
		string refreshOutSet = argv[3];
		string refreshFiles = argv[4];

		if (fs::exists(refreshOutSet + "/dirs.txt"))
			logger::fatal("Output set exists");

		// Get files path & check it exists
		fs::path inset = checkSet(refreshInSet, false);
		fs::path outset = checkSet(refreshOutSet, true);
		fs::path refresh = checkSet(refreshFiles, false);


		// load the ImgCollection from disk into a ImgCollectionBuilder
		ImgCollectionRefresh* sb = new ImgCollectionRefresh();
		Timer::start();
		sb->Create(outset);
		sb->Load(inset);
		sb->Refresh(refresh);
		sb->Save();
	}
	else
		usage();

	// print some stats
	logger::info(Timer::report());
	FreeImage_DeInitialise();


    return 0;


}

