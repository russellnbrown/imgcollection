// MathLibrary.cpp : Defines the exported functions for the DLL.
#include "pch.h" // use stdafx.h in Visual Studio 2017 and earlier
#include <utility>
#include <limits.h>
#include "cpp_library_dll.h"
#include "cpp_library.h"
#include <vector>

// DLL internal state variables:


//static std::vector<matchingItem*> results;
static std::list<matchingItem*> results;

void clearresults()
{
	for (auto x = results.begin(); x != results.end(); x++)
		delete* x;
	results.clear();
}

CIMAGELIB cimageinit(const char* set)
{
	try
	{
		clearresults();
		icLib* ci = new icLib();
		ci->icload(set);
		return ci;
	}
	catch (std::runtime_error * e)
	{
		std::cerr << "problem in init:" << e->what() << std::endl;
		return nullptr;
	}
}

int cimagefree(CIMAGELIB _ci)
{
	clearresults();
	icLib*ci = (icLib*)_ci;
	ci->icclose();
	return 0;
}


int cimagesearch(CIMAGELIB _ci, const char *find, const char *algo)
{
	try
	{
		clearresults();
		results = ((icLib*)_ci)->icfind(find, algo);
		std::cout << "dllResult count=" << results.size() << std::endl;
		return (int)results.size();
	}
	catch (std::runtime_error *e)
	{
		std::cerr << "problem in search:" << e->what() << std::endl;
		return -1;
	}
}


int cimageresultfilecount(CIMAGELIB c, int rx)
{
	if ( rx < 0 || rx >= results.size() )
		return 0;
	auto rxi = std::next(results.begin(), rx);
	return  (*rxi)->files.size();

}

double cimageresultcloseness(CIMAGELIB c, int rx)
{
	if (rx < 0 || rx >= results.size())
		return 0;
	auto rxi = std::next(results.begin(), rx);
	return  (*rxi)->closeness;
}

const char* cimageresultfile(CIMAGELIB c, int rx, int fx)
{
	if (rx < 0 || rx >= results.size())
		return 0;
	auto rxi = std::next(results.begin(), rx);

	if (fx < 0 || fx >= (*rxi)->files.size())
		return 0;

	return  std::next((*rxi)->files.begin(), fx)->c_str();
	
}


