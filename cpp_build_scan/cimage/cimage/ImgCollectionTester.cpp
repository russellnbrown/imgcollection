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


 // ImgCollectionBuilder. This is a helper class to build an Image Collection.
 // It can use a simple or multithreaded approach.


// constructor. set the singleton
ImgCollectionTester::ImgCollectionTester()
{
}

int dirs = 0;
int files = 0;


bool ImgCollectionTester::walkDirecrories(fs::path _dir)
{

	string dirpart;
	string filepart;
	time_t lastmod = 0;
	bool processFiles = false;

	finfo dir(_dir);


	logger::info("Dir: " + dir.full.string());


	// go thru all files/dirs
	for (fs::directory_entry de : fs::directory_iterator(dir.full))
	{
		finfo fi(de.path());

		if (fi.isDir)
		{
			walkDirecrories(fi.full);
		}
		else
		{
			logger::debug("Fil: " + fi.full.string());
		}
	}


	return true;
}



void ImgCollectionTester::Test(int argc, char *argv[])
{
	// Save the set to db
	Timer::start();
	fs::path dir(argv[2]);
	walkDirecrories(dir);
	Timer::stop("Saving ");
}

