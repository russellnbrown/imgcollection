// c_test.cpp : This file contains the 'main' function. Program execution begins and ends there.
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
