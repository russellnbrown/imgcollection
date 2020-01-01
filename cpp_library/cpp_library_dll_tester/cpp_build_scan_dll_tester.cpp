// cimagedlltester.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>

#include "cpp_library_dll.h"


int main()
{
	CIMAGELIB tag = cimageinit("C:\\TestEnvironments\\img\\clib.db");
	int nresults = cimagesearch(tag, "C:\\TestEnvironments\\img\\tvs\\5\\fred0.jpg", "simple");
	for (int c = 0; c < nresults; c++)
	{
		std::cout << "result " << c << std::endl;
		int nfiles = cimageresultfilecount(tag, c);
		std::cout << "files " << nfiles << std::endl;
		int close = cimageresultcloseness(tag, c);
		std::cout << "close " << close << std::endl;
		for (int fc = 0; fc < nfiles; fc++)
		{
			const char* str = cimageresultfile(tag, c, fc);
			std::cout << "file " << str << std::endl;
		}
	}
	std::cout << "Result count=" << nresults << std::endl;
	cimagefree(tag);


}