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
 // SetSearch.c : functions to search an Image Collection
 //

#include "common.h"

// Searcher is used in hashmap iterator rather than using a global
typedef struct _Searcher
{
	// results - used to save search results
	ImageSearchResult* results;
	// srchImage - the image to search for
	ImageInfo* srchImage;
}Searcher;

// Some pinched simple sort algos to sort the final result by 'closeness'
// http://www.firmcodes.com/c-program-to-sorting-a-singly-linked-list/
void search_resultSwap(ImageSearchResult* p1, ImageSearchResult* p2)
{
	SetItemImage* ti = p1->i;
	double tc = p1->closeness;

	p1->i = p2->i;
	p1->closeness = p2->closeness;

	p2->i = ti;
	p2->closeness = tc;
}

void search_resultSort(ImageSearchResult* head)
{
	ImageSearchResult* start = head;
	ImageSearchResult* traverse;
	ImageSearchResult* min;

	if (head == NULL)
		return;

	while (start->next)
	{
		min = start;
		traverse = start->next;

		while (traverse)
		{
			/* Find minimum element from array */
			if (min->closeness > traverse->closeness)
			{
				min = traverse;
			}

			traverse = traverse->next;
		}
		search_resultSwap(start, min);			// Put minimum element on starting location
		start = start->next;
	}
}

// search_findFirstFile - search files for a matching image hash
SetItemFile* search_findFirstFile(Set *s, uint32_t hash)
{
	// return first we find ( there may be others )
	for (SetItemFile* sif = s->files; sif != NULL; sif = sif->next)
		if (sif->ihash == hash)
			return sif;
	return NULL;
}

// search_findDirectory - search directories for a matching dhash
SetItemDir* search_findDirectory(Set *s, uint32_t dhash)
{
	for (SetItemDir* sid = s->dirs; sid != NULL; sid = sid->next)
		if (sid->dhash == dhash)
			return sid;
	return NULL;

}

Searcher* srch;
MUTEXHANDLE srchProtect;

void search_addResult(ImageSearchResult* iss)
{
	if (srchProtect!=NULL)
		mutex_lock(srchProtect);
	iss->next = srch->results;
	srch->results = iss;
	if (srchProtect!=NULL)
		mutex_unlock(srchProtect);
}

// search_compareImages - used as a callback fot the hah map iterator
void search_compareImages(void *q1)
{
	// the image in the iteration
	SetItemImage* sii = (SetItemImage*)q1;
	// searcher is used to store results & image being searched for
	//Searcher* srch = (Searcher*)q1;

	double cv;
	
	// if image hashes match, this is an exact match, no need to comapre
	if (sii->ihash == srch->srchImage->crc)
		cv = 0;
	else // otherwise calc closeness
		cv = iutil_compare(sii->tmb, srch->srchImage->thumb);

	// only consider close'ish files, pointless saving others 
	if (cv < SEARCH_CLOSENESS_THRESHOLD)
	{
		// Make a search result and add to list
		ImageSearchResult* iss = malloc(sizeof(ImageSearchResult));
		if (iss)
		{
			iss->closeness = cv;
			iss->i = sii;
			search_addResult(iss);
		}
	}


}

void *searchThreadFunc(void * param)
{
	SrchThreadInfo* sti = (SrchThreadInfo*)param;
	logger(Info, "Created thread %d", sti->threadNum);
	for (SetItemImage* si = sti->images; si != NULL; si = si->trdnext)
	{
		search_compareImages(si);
		sti->numProcessed++;
	}
	logger(Info, "Thread %d finished, processed %d images.", sti->threadNum, sti->numProcessed);
	return 0;
}

void run_threads(Set *s, Searcher *srch)
{
	// Create ther search threads. These will process all the images in their
	// input Q. Each has same number to process rather than a single feed Q as
	// we used with encoding. this is because procesing time should be identical
	// and its quicker than maintaining a synced Q. 
	// The searchThreadFunc is the same function used whne threads not being used
	// Results go into 'srch' Q
	for (int tx = 0; tx < s->numThreads; tx++)
	{
		s->threads[tx].threadId = thread_start((THREADFUNC)searchThreadFunc, (THREADPAR)&s->threads[tx]);
	}
	// wait for them to finish
	for (int tx = 0; tx < s->numThreads; tx++)
	{
		thread_wait(s->threads[tx].threadId);
	}
}

// search - searches for the image 'file' in the set 'set'. 
void search(char *set, char *file, BOOL useThreads)
{
	// quick checks

	if (!util_directoryExists(set))
		logger(Fatal, "Set does not exist: %s", set);

	if (!util_fileExists(file))
		logger(Fatal, "File does not exist: %s", file);

	// Searcher is used to hold image to be searched for and the
	// results of the search
	srch = malloc(sizeof(Searcher));
	if (!srch)
	{
		logger(Fatal, "Couldnt create a searcher");
		return;
	}
	memset(srch, 0, sizeof(Searcher));
	// if using threads, we will need to protect srch with a mutex
	if (useThreads)
		srchProtect = mutex_get();


	// get image thumbnail & hash etc and put in searcher
	SplitPath* sp = util_splitPath(file);
	srch->srchImage = iutil_getImageInfo(0, file, sp);
	util_freeSplitPath(sp);

	// Now we load the set from disk
	Timer* t = timer_createx();
	timer_start(t);
	Set *s = set_create(useThreads);
	if (!set_load(s, set))
		return;
	timer_stop(t);
	double loadTime = timer_getElapsedTimeInMilliSec(t);

	// Now we do the actual search
	timer_start(t);
	// Search the images either using tree traversal if not using threads or
	// the list of images in the threadcontrol object	
	if (s->useThreads)
		run_threads(s, srch);
	else
		tree_inorder(s->imageTree, search_compareImages);

	// finally sort list by closeness
	search_resultSort(srch->results);
	timer_stop(t);
	double searchTime = timer_getElapsedTimeInMilliSec(t);

	logger(Info, "Times: load %.2f ms., search %.2f ms.", loadTime, searchTime);

	// print results
	for (ImageSearchResult* ir = srch->results; ir != 0; ir = ir->next)
	{
		// more than one file may match a single image if they are duplicates, so we need to iterate through
		// all images 
		int found = 0;
		for (SetItemFile* sif = s->files; sif != NULL; sif = sif->next)
		{
			// matching file ?
			if (sif->ihash == ir->i->ihash )
			{
				found++;
				SetItemDir* sid = search_findDirectory(s, sif->dhash);
				if (sid)
					logger(Info, "Sorted - , image %u, closeness %.2f file %s in %s", ir->i->ihash, ir->closeness, sif->name, sid->path);
				else
					logger(Info, "Sorted - , image %u, closeness %.2f file %s in <no matching dir>", ir->i->ihash, ir->closeness, sif->name);
			}
		}
		// if found = 0 the nothing found
		if ( found == 0 )
			logger(Info, "Sorted - , image %u, closeness %.2f <no matching file>", ir->i->ihash, ir->closeness);
	}

}


