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
 // SetBuilder.h - methods to create the 'Image Database'
 //

// threadControl
//
// used to keep data relevant to a thread. An array 'threads' is
// created when starting the threads
//
struct threadControl
{
	THREADHANDLE tid;	// it's id
	int tix;			// it's index (0..numThreads)
	Set* s;				// pass it the set
	int numProcessed;	// number of images processed
}*threads;

// threadItem
//
// used to pass information about image file to build threads. We have a Q of
// these, one for each file, the threads read off this Q when ready 
// for the next file to process. Protected with 'tlock'
//
struct threadItem
{
	uint32_t dhash;
	char* ipath;
	struct threadItem* next;
};

// SetBuilderInfo
//
// Used to hold all info relating to the build
//
typedef struct _SetBuilderInfo
{
	Set* s;				// Set will be created in 's'
	BOOL running;		// signal threads when we are finished

	// thread feeder Q, 
	// we will place at end and remove from front
	struct threadItem* threadListHead;
	struct threadItem* threadListTail;
	int threadListLen;
	
	// mutexes for protecting structures used bt threads. tlock
	// protects thread feeder Q and rlock protects lists in the 
	// set
	MUTEXHANDLE tlock;
	MUTEXHANDLE rlock;	

}SetBuilderInfo;

SetBuilderInfo* setbuild_makeSetBuilderInfo();
void setbuild_freeSetBuilderInfo(SetBuilderInfo*);
void setbuild_create(char*, char*, BOOL);
int setbuild_winscan(const char*);
uint32_t setbuild_processDirectory(const char*);
int setbuild_processImageFile(uint32_t, const char*);
void setbuild_waitthreads();
void setbuild_startthreads();
THREADRETURN setbuild_threadRun(THREADPAR);
void setbuild_addToThreadQ(uint32_t, const char*);
struct threadItem* setbuild_makeThreadItem();
void setbuild_freeThreadItem(struct threadItem* ti);