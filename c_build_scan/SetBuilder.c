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

SetBuilderInfo* setbuilder = NULL;

SetBuilderInfo *setbuild_makeSetBuilderInfo()
{
	SetBuilderInfo* sbi = malloc(sizeof(SetBuilderInfo));
	if (sbi)
	{
		memset(sbi, 0, sizeof(SetBuilderInfo));
		sbi->running = TRUE;
		return sbi;
	}
	logger(Fatal, "Ran out of memory allocating SetBuilderInfo");
}

void setbuild_freeSetBuilderInfo(SetBuilderInfo*sbi)
{
	free(sbi->s);
	free(sbi);
}

struct threadItem *setbuild_makeThreadItem()
{
	struct threadItem* ti = malloc(sizeof(struct threadItem));
	if (ti)
	{
		memset(ti, 0, sizeof(struct threadItem));
		return ti;
	}
	logger(Fatal, "Ran out of memory allocating threadItem");
}

void setbuild_freeThreadItem(struct threadItem* ti)
{
	free(ti->ipath);
	free(ti);
}

// set_addToThreadQ
// this is called to add a file to the build thread queue
void setbuild_addToThreadQ(uint32_t dhash, const char* ipath)
{
	// create a threadItem for this file
	struct threadItem* ti = setbuild_makeThreadItem();
	ti->ipath = strdup(ipath);
	ti->dhash = dhash;

	// add it to the tail of the thread queue. If Q is too long then
	// just wait until it shrinks as items are processes by the threads.
	// This prevents it get too big as wile walker will be much faster than
	// file processing
	BOOL placed = FALSE;
	int inq = 0;
	while (!placed)
	{
		mutex_lock(setbuilder->tlock);
		if (setbuilder->threadListLen < (setbuilder->s->numThreads*100) )
		{
			if (setbuilder->threadListTail != NULL)
				setbuilder->threadListTail->next = ti;
			setbuilder->threadListTail = ti;
			if (setbuilder->threadListHead == NULL)
				setbuilder->threadListHead = setbuilder->threadListTail;
			inq = setbuilder->threadListLen++;

			placed = TRUE;
			mutex_unlock(setbuilder->tlock);
			//logger(Debug, "TRDDIST added %s to Q, size now %d", ipath, inq);

		}
		else
		{
			mutex_unlock(setbuilder->tlock);
			util_msleep(100); // quick nap before trying again
		}
	}
}

// threadRun
// this is the builder thread. basically it performs the same function as
// processImageFile but in a thread, reading the thread input Q for files
// to process
THREADRETURN setbuild_threadRun(THREADPAR p)
{
	struct threadControl* tc = (struct threadControl*)p;

	// we loop until there is nothing left to process & running has been set to false in set_threadsstop()
	while(TRUE)
	{
		// Get next item to process from head of Q
		mutex_lock(setbuilder->tlock);
		// remove from the head
		struct threadItem* item = setbuilder->threadListHead;
		if (item != NULL)
		{
			// adjust pointers to remove item from Q
			setbuilder->threadListHead = item->next;
			setbuilder->threadListLen--;
		}
		mutex_unlock(setbuilder->tlock);

		// nothing in list, just wait and repeat unless running was set to FALSE 
		if (item == NULL)
		{
			if (!setbuilder->running )
			{
				logger(Info, "TRD[%d] is finished, processed %d images", tc->tix, tc->numProcessed);
				return THREADRETURNOK;
			}
			util_msleep(10);
			continue;
		}

		// process the file & then repeat
		tc->numProcessed++;
		SplitPath* sp = util_splitPath(item->ipath);

		ImageInfo* ii = iutil_getImageInfo(setbuilder->s, item->ipath, sp);
		if (ii)
		{
			mutex_lock(setbuilder->rlock);
			SetItemFile* f = set_addFile(setbuilder->s, item->dhash, ii->crc, sp->fullfile);
			SetItemImage* sii = set_addImage(setbuilder->s, ii);
			mutex_unlock(setbuilder->rlock);
		}
		setbuild_freeThreadItem(item);
		util_freeSplitPath(sp);

	}
	
}

// set_startthreads
//
// starts the building threads
//
void setbuild_startthreads()
{
	// Start the build procssing threads. 

	// create thread input Q protection lock
	setbuilder->tlock = mutex_get();
	setbuilder->rlock = mutex_get();

	// create threadControl items
	threads = malloc(sizeof(struct threadControl) * setbuilder->s->numThreads);
	memset(threads, 0, sizeof(struct threadControl) * setbuilder->s->numThreads);

	// sart the threads
	for (int tx = 0; tx < setbuilder->s->numThreads; tx++)
	{
		threads[tx].s = setbuilder->s;
		threads[tx].tix = tx;
		threads[tx].tid = thread_start(setbuild_threadRun, &threads[tx]);
		logger(Info, "TCTRL started thread %d ID is %d", tx, threads[tx].tid);
	}
}

// set_waitthreads
//
// wait for all threads to stop
//
void setbuild_waitthreads()
{
	logger(Info, "Waiting for threads to close");

	for (int tx = 0; tx < setbuilder->s->numThreads; tx++)
		thread_wait(threads[tx].tid);

	logger(Info, "All closed, left=%d", setbuilder->threadListLen);

}


// processImageFile - get hashes & thumbs for an image and store in a SetItemImage and SetItemFile
// put those into the set
int setbuild_processImageFile(uint32_t dhash, const char* ipath)
{
	static int processed = 0;

	if ( ++processed%1000 == 0 )
	{
		logger(Info, "At file %d", processed);		
	}

	// if using threads, call set_addToThreadQ which will add the file to
	// the build thread Q
	if (setbuilder->s->useThreads)
	{
		setbuild_addToThreadQ(dhash, ipath);
		return 0;
	}

	// otherwise we will just process the file
	SplitPath* sp = util_splitPath(ipath);

	ImageInfo* ii = iutil_getImageInfo(setbuilder->s, ipath, sp);
	if (ii)
	{
		SetItemFile* f = set_addFile(setbuilder->s, dhash, ii->crc, sp->fullfile);
		SetItemImage* sii = set_addImage(setbuilder->s, ii);
	}

	util_freeSplitPath(sp); 

	return 0;
}

// processDirectory - calculate dhash, creates a SetItemDir and stores in the set
uint32_t setbuild_processDirectory(const char* dpath)
{
	char rel[MAX_PATH];
	set_relativeTo(setbuilder->s, dpath, rel);
	uint32_t dhash = 0;
	util_crc32(rel, strlen(rel), &dhash);
	SetItemDir* d = set_addDir(setbuilder->s, rel, dhash);

	return dhash;
}

// Directory tree traversal radically different between linux and windows so have two
// completly different bits of code to do that. Both versions call processImageFile & 
// processDirectory as needed

#ifdef WIN32

int setbuild_winscan(const char* thisdir)
{
	WIN32_FIND_DATA ff;

	// process the directory we are in
	uint32_t dhash = setbuild_processDirectory(thisdir);

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
				setbuild_winscan(thisfile);
		}
		else
		// else its a file
		{
			setbuild_processImageFile(dhash, thisfile);
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
int  setbuild_processEntry(const char* item, const struct stat* info, const int typeflag, struct FTW* pathinfo)
{
	
	char filepath[MAX_PATH];
	util_pathInit(filepath);
	strncpy(filepath, item, MAX_PATH);

	// if file & imagefile call processImageFile
	if (typeflag == FTW_F)
	{
		if (util_isImageFile(filepath))
		{
			setbuild_processImageFile(dhash, filepath);
			//logger(Info, "IMAGEFILE %s", filepath);
		}
		//else
		//	logger(Info, "FILE %s", filepath);
	}
	// else, if a dir, call processDirectory
	else if (typeflag == FTW_D || typeflag == FTW_DP)
	{
		dhash = setbuild_processDirectory(filepath);
		//logger(Info, "DIR %s", filepath);
	}


	return 0;
}

#endif

// create - this will create the Image Collection under Set 's'
void  setbuild_create(char* set, char* dir, BOOL useThreads)
{
	setbuilder = setbuild_makeSetBuilderInfo();

	Timer* t = timer_createx();

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
	setbuilder->s = set_create(useThreads);
	set_setTop(setbuilder->s, pwd);

	if (useThreads)
		setbuild_startthreads();

	// Start the directory scan using appropriate scanner

#ifdef WIN32
	int result = setbuild_winscan(pwd);
#else
	int result = nftw(pwd, setbuild_processEntry, 20, FTW_PHYS);
#endif

	if (result != 0)
		logger(Fatal, "Scan failed %d", result);

	if (useThreads)
	{
		logger(Info, "TCTRL Files processed, running set to false ");
		setbuilder->running = FALSE;
		setbuild_waitthreads();
		logger(Info, "TCTRL Threads stopped");
	}

	// save set
	timer_stop(t);
	double createTime = timer_getElapsedTimeInMilliSec(t);

	set_printStats(setbuilder->s);
	timer_start(t);
	set_save(setbuilder->s, set);
	timer_stop(t);
	double saveTime = timer_getElapsedTimeInMilliSec(t);

	logger(Info, "Times: create %.2f ms., save %.2f ms.", createTime, saveTime);

}



