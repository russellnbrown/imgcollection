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


 //
 // checkExists
 //
 // Check if a file exists and return a path
 //
optional<fs::path> checkExists(string spath)
{
	try
	{
		fs::path files(spath);
		if (!fs::exists(files))
		{
			logger::error("File " + files.string() + " does not exist");
			return nullopt;
		}
		return files;
	}
	catch (std::exception e)
	{
		logger::excep("Exeption in checkExists: ", e);
		return nullopt;
	}
}
//
// checkFiles
//
// Check if a file exists and return a path
//
optional<fs::path> checkFiles(string spath)
{
	try
	{
		fs::path files(spath);
		files = fs::canonical(files);
		if (!fs::exists(files))
		{
			logger::error("File " + files.string() + " does not exist");
			return nullopt;
		}
		return files;
	}
	catch (std::exception e)
	{
		logger::excep("Exeption in checkFiles: ", e);
		return nullopt;
	}
}

//
// checkSet
//
// Check if a set's directory exists, optionally create it if flag is set
//
optional<fs::path> checkSet(string spath, bool createIfNeeded)
{
	try
	{
		// check if it exists and is a dirctory
		fs::path set(spath);
		if (fs::exists(set))
		{
			if (!fs::is_directory(set))
			{
				logger::error("Path " + set.string() + " exists and isnt a directory");
				return nullopt;
			}
		}
		else if (createIfNeeded)
		{
			logger::info("Creating Path " + set.string());
			fs::create_directory(set);
			logger::info("Created Path " + set.string());
		}
		else
		{
			logger::error("Path " + set.string() + " dosn't exist and isnt set to be created.");
			return nullopt;
		}

		return set;
	}
	catch (std::exception e)
	{
		logger::excep("Exeption in checkSet: ", e);
		return nullopt;
	}

}

icUtils::SearchAlgo getTypeFromStr(string& s)
{
	if (s == "simple") return icUtils::SearchAlgo::Simple;
	if (s == "luma") return icUtils::SearchAlgo::Luma;
	if (s == "asm") return icUtils::SearchAlgo::Assembler;
	if (s == "mono") return icUtils::SearchAlgo::Mono;
	logger::error("Unknown search method: " + s + " defaulting to Simple");
	return icUtils::SearchAlgo::Simple;
}


int parsehostport(string &hp)
{
	int port = 3306;
	string host = hp;

	size_t cp = host.find_first_of(':');
	if (cp > 0)
	{
		port = atoi(host.substr(cp+1).c_str());
		hp = host.substr(0, cp);
	}
	return port;
}

#ifdef USEDB

bool icLib::ictestdb(std::string shostport, std::string user, std::string pass, std::string dbase)
{
	string host = shostport;
	int port = parsehostport(host);
	return icBuilder::Test(host, port, dbase, user, pass );
}

bool icLib::iccreate(std::string shostport, std::string user, std::string pass, std::string dbase, std::string sfiles)
{
	// Initialize the free image library

	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// Get files & db path & check it exists - return false if not
	optional<fs::path> files = checkFiles(sfiles);
	unique_ptr<icBuilder> sb = make_unique<icBuilder>();

	string host = shostport;
	int port = parsehostport(host);

	// Add all files to the Collection
	Timer::start();
	coll = sb->Create(sfiles);
	Timer::stop("Scaning");

	// Save the set to db
	Timer::start();
	sb->Save(coll, host, port, dbase, user, pass);
	Timer::stop("Saving ");
	logger::info("Timings:\n" + Timer::report());
	return true;
}

list<matchingItem*> icLib::icsearch(std::string shostport, std::string user, std::string pass, std::string dbase, std::string sfind, std::string algo)
{
	// Initialize the free image library

	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// Get files & db path & check it exists - return false if not
	optional<fs::path> files = checkFiles(sfind);
	unique_ptr<icLoader> sb = make_unique<icLoader>();

	string host = shostport;
	int port = parsehostport(host);

	// Add all files to the Collection
	Timer::start();
	coll = sb->Load(host, port, dbase, user, pass);
	Timer::stop("Loading");

	// Save the set to db
	Timer::start();
	icSearch s(coll, getTypeFromStr(algo));
	Timer::stop("Searched");
	logger::info(Timer::report());
	return std::move(s.Find(*files));
}
#endif



bool icLib::iccreate(string sset, string sfiles)
{
	// Initialize the free image library

	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// Get files & db path & check it exists - return false if not
	optional<fs::path> files = checkFiles(sfiles);

	optional<fs::path> set = checkSet(sset, true);
	if (!(files && set))
		return false;

	unique_ptr<icBuilder> sb = make_unique<icBuilder>();

	// Add all files to the Collection
	Timer::start();
	coll = sb->Create(*files);
	Timer::stop("Scaning");

	// Save the set to db
	Timer::start();
	//coll->Save("localhost", "imgdb", "imgdb");
	sb->Save(coll, *set);
	Timer::stop("Saving ");
	logger::info("Timings:\n" + Timer::report());
	return true;
}

list<matchingItem*> icLib::icsearch(std::string set, std::string sfind, std::string algo)
{
	// Initialize the free image library

	FreeImage_Initialise(TRUE);
	logger::info("Using FreeImage version " + string(FreeImage_GetVersion()));

	// Get files & db path & check it exists - return false if not
	optional<fs::path> find = checkExists(sfind);

	// Add all files to the Collection
	Timer::start();
	icLoader l;
	coll = l.Load(set);
	Timer::stop("Loading");
	Timer::start();
	icSearch s(coll, getTypeFromStr(algo));
	Timer::stop("Searched");
	logger::info(Timer::report());
	return std::move(s.Find(*find));

}


bool icLib::icload(std::string set)
{
	// Initialize the free image library

	FreeImage_Initialise(TRUE);
	icLoader l;
	coll = l.Load(set);
	return true;

}

list<matchingItem*> icLib::icfind(std::string sfind, std::string algo)
{
	// Get files & db path & check it exists - return false if not
	optional<fs::path> find = checkExists(sfind);
	icSearch s(coll, getTypeFromStr(algo));
	return std::move(s.Find(*find));

}

bool icLib::icclose()
{
	if (coll)
		delete coll;
	return true;
}
