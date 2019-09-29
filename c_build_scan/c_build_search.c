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

//
// c_build_search.c : The main function and freeimage initialization
//

#include "common.h"


int initFreeImage()
{
	FreeImage_Initialise(FALSE);
	const char* v = FreeImage_GetVersion();
	printf("FreeImage version is %s\n", v);
	return 0;
}

int cleanupFreeImage()
{
	FreeImage_DeInitialise();
	return 0;
}

void usage()
{
	printf("usage: c_build_search [-c <img collection> <root dir>|-s <img collection> <file to find>\n");
	return;
}

int main(int argc, char* argv[])
{

	// start logging
	logto("c_build_search.log");

	// inti freeimage lib
	initFreeImage();


	// check args to see what to do -c create, -s srarch
	if (argc == 4 && strcmp(argv[1], "-c") == 0)
		create(argv[2], argv[3]);
	else if (argc == 4 && strcmp(argv[1], "-s") == 0)
		search(argv[2], argv[3]);
	else
		usage();

	// clean up
	cleanupFreeImage();
	return 0;
}
