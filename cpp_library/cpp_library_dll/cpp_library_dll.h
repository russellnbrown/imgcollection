#pragma once

#include <string>
#include <iostream>
#include <list>

class dllmatchingItem
{
public:
	double			closeness;
	std::list<std::string>	files;
};


#ifdef MATHLIBRARY_EXPORTS
#define MATHLIBRARY_API __declspec(dllexport)
#else
#define MATHLIBRARY_API __declspec(dllimport)
#endif

#define CIMAGELIB void *


//extern "C" MATHLIBRARY_API bool iccreate(std::string set, std::string files);
extern "C" MATHLIBRARY_API int cimagesearch(CIMAGELIB c, const char *find, const char *algo);
extern "C" MATHLIBRARY_API CIMAGELIB cimageinit(const char *set);
extern "C" MATHLIBRARY_API int cimagefree(CIMAGELIB c);

extern "C" MATHLIBRARY_API int cimageresultfilecount(CIMAGELIB c, int rx);
extern "C" MATHLIBRARY_API double cimageresultcloseness(CIMAGELIB c, int rx);
extern "C" MATHLIBRARY_API const char *cimageresultfile(CIMAGELIB c, int rx, int fx);

