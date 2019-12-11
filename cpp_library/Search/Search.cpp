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



#include <string>
#include <iostream>
#include "cpp_library.h"
#include "cmdoptions.h"

using namespace std;

/* 
-s set.db -l realfiles
-d imgcollection -h localhost:3306 -u imgdb -p imgdb -l realfiles
-s set.db -f file_5.png
-d imgcollection -h localhost:3306 -u imgdb -p imgdb -f file_5.png
*/

//
// usage 
//
// called when someting wrong with command line arguments, missing files etc.
//
void usage(const string reason)
{
	if (reason.length() > 0)
		cerr << "Error: " << reason << endl;

#ifdef USEDB
	cerr << "usage: -f <find file path> [-m simple|luma|asm|mono] [ -s <set> | -d <db> -u <user> -p <pass> -h <host>[:<port>] ]" << endl;
#else
	cerr << "usage: -f <find file path> [-m simple|luma|asm|mono] -s <set>" << endl;
#endif
	cerr << "where:-" << endl;
	cerr << " <find file path> : the file to search for" << endl;
	cerr << " simple|luma|asm|mono : the seach algorithm" << endl;
	cerr << " <set> : The collection ( directory containing flat files )" << endl;
#ifdef USEDB
	cerr << " <db><user><pass><host><port> : The collection ( DB access parameters )" << endl;
#endif
	exit(0); // give up !
}

int main(int argc, char* argv[])
{

	//
	// Print quick identifier for binary version
	//
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

	// parse command line
	CmdOptions cmd(argc, argv);

	// we can use either a real database or flatfiles to save the image
	// collection - use one of the following:-

	// -s <set> -f <find file path>                                        // for flat files 
	// -d <db> -u <user> -p <pass> -h <host>[:<port>] -f <find file path>  // for real database

	// both versions require a file to find
	optional<string> sfind = cmd.getflag("-f");
	if (!sfind)
		usage("No find file (-f) specified");

	std::list<matchingItem*> results;

	string algo = "simple";
	optional<string> salgo = cmd.getflag("-m");
	if (salgo)
		algo = *salgo;

	// check which version to run...
	if (cmd.getflag("-s")) // Flat Files
	{
		optional<string> sset = cmd.getflag("-s");
		if ((sset && (*sset).length() && sfind))
			results = icsearch(*sset, *sfind, algo);
		else
			usage("-s option is missing one or more required parameters");
	}
#ifdef USEDB
	else if (cmd.getflag("-d")) // DataBase
	{
		optional<string> db = cmd.getflag("-d");
		optional<string> suser = cmd.getflag("-u");
		optional<string> spass = cmd.getflag("-p");
		optional<string> shost = cmd.getflag("-h");
		// host may be omitted in which case we use default
		// values on local host
		if (!shost)
			shost = "localhost:3306";
		if ((db && sfind && spass && shost && suser))
		{
			// test connection to DB before continuing
			if (ictestdb(*shost, *suser, *spass, *db))
				results = icsearch(*shost, *suser, *spass, *db, *sfind, algo);
		}
		else
			usage("-d option is missing one or more required parameters");
	}
#endif
	else
		usage("");

	for (std::list<matchingItem*>::iterator ri = results.begin(); ri != results.end(); ri++)
	{
		cout << "Closeness: " << to_string((*ri)->closeness) << endl;
		for (std::list<std::string>::iterator pi = (*ri)->files.begin(); pi != (*ri)->files.end(); pi++)
		{
			cout << "  file: " << (*pi) << endl;
		}
	}

	return 0;
}

