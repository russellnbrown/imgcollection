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



int main(int argc, char* argv[])
{

	logto("capp.log");

	initFreeImage();


	if (argc == 4 && strcmp(argv[1], "-c") == 0)
	{
		create(argv[2], argv[3]);
	}


	cleanupFreeImage();

	return 0;
}
