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
 // SetBuilder.c : functions to buils an Image Collection aka Set
 //

#include "common.h"

// Set will be created in 's'
Set* s = NULL;

// processImageFile - get hashes & thumbs for an image and store in a SetItemImage and SetItemFile
// put those into the set
int processImageFile(uint32_t dhash, const char* ipath)
{
	SplitPath* sp = util_splitPath(ipath);

	ImageInfo* ii = iutil_getImageInfo(s, ipath);
	if (ii)
	{
		SetItemFile* f = set_addFile(s, dhash, ii->crc, sp->fullfile);
		SetItemImage* sii = set_addImage(s, ii);
	}

	util_freeSplitPath(sp);

	return 0;
}

// processDirectory - calculate dhash, creates a SetItemDir and stores in the set
uint32_t processDirectory(const char* dpath)
{
	char rel[MAX_PATH];
	set_relativeTo(s, dpath, rel);
	uint32_t dhash = 0;
	util_crc32(rel, strlen(rel), &dhash);
	SetItemDir* d = set_addDir(s, rel, dhash);

	return dhash;
}

// Directory tree traversal radically different between linux and windows so have two
// completly different bits of code to do that. Both versions call processImageFile & 
// processDirectory as needed

#ifdef WIN32

int winscan(const char* thisdir)
{
	WIN32_FIND_DATA ff;

	// process the directory we are in
	uint32_t dhash = processDirectory(thisdir);

	// set up a wildcard scan path
	char scandir[MAX_PATH];
	util_pathInit(scandir);
	strcat(scandir, thisdir);
	strcat(scandir, "/*");

	// get handle to start of scan
	HANDLE fh = FindFirstFileA(scandir, &ff);
	int next = -1;

	if (fh == INVALID_HANDLE_VALUE)
		logger(Fatal, "Can't start FindFirstFile");

	do
	{
		// make file into full path
		char thisfile[MAX_PATH];
		util_pathInit(thisfile);
		snprintf(thisfile, MAX_PATH, "%s/%s", thisdir, ff.cFileName);

		// if its's a directory, call ourselves recursivly
		if (ff.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			if (strcmp(ff.cFileName, ".") != 0 && strcmp(ff.cFileName, "..") != 0)
				winscan(thisfile);
		}
		else
			// else its a file
		{
			processImageFile(dhash, thisfile);
		}

		// continue with next dir entry
		next = FindNextFileA(fh, &ff);
	} while (next != 0);

	// all done, free handle
	FindClose(fh);

	return 0;
}

#else

// linux uses ftw methods to do the scan

#define _XOPEN_SOURCE 700
#define __USE_XOPEN_EXTENDED
#include <ftw.h>

uint32_t dhash = 0;

// process_entry called automatically by nftw for each file/directory ( directories  first )
int process_entry(const char* item, const struct stat* info, const int typeflag, struct FTW* pathinfo)
{
	
	char filepath[MAX_PATH];
	util_pathInit(filepath);
	strncpy(filepath, item, MAX_PATH);

	// if file & imagefile call processImageFile
	if (typeflag == FTW_F)
	{
		if (util_isImageFile(filepath))
		{
			processImageFile(dhash, filepath);
			//logger(Info, "IMAGEFILE %s", filepath);
		}
		//else
		//	logger(Info, "FILE %s", filepath);
	}
	// else, if a dir, call processDirectory
	else if (typeflag == FTW_D || typeflag == FTW_DP)
	{
		dhash = processDirectory(filepath);
		//logger(Info, "DIR %s", filepath);
	}


	return 0;
}

#endif

// create - this will create the Image Collection under Set 's'
void create(char* set, char* dir)
{

	Timer* t = timer_create();

	// need to convert file location to abs path so we can get
	// a correct set 'top' location later
	char apath[MAX_PATH];
	util_absPath(apath, dir);

	logger(Info, "Path given is %s, Set is %s", dir, set);

	if (!util_directoryExists(dir))
	{
		logger(Fatal, "Dir dosnt exist %s ( resolved to %s)", dir, apath);
	}

	// standardize path
	char pwd[MAX_PATH];
	util_standardizePath(pwd, apath);

	// make the directory to hold the set if needed
	if (!util_directoryExists(set))
	{
		logger(Info, "Set dir dosn't exist, making %s", set);
		util_mkdir(set);
	}

	// create the set structure & set 'top'
	timer_start(t);
	s = set_create();
	set_setTop(s, pwd);

	// Start the directory scan using appropriate scanner

#ifdef WIN32
	int result = winscan(pwd);
#else
	int result = nftw(pwd, process_entry, 20, FTW_PHYS);
#endif

	if (result != 0)
		logger(Fatal, "Scan failed %d", result);

	// save set
	timer_stop(t);
	double createTime = timer_getElapsedTimeInMilliSec(t);

	set_printStats(s);
	timer_start(t);
	set_save(s, set);
	timer_stop(t);
	double saveTime = timer_getElapsedTimeInMilliSec(t);

	logger(Info, "Times: create %.2f ms., save %.2f ms.", createTime, saveTime);

}



