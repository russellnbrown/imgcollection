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

// search_compareImages - used as a callback fot the hah map iterator
int search_compareImages(any_t q1, any_t q2)
{
	// the image in the iteration
	SetItemImage* sii = (SetItemImage*)q2;
	// searcher is used to store results & image being searched for
	Searcher* srch = (Searcher*)q1;

	double cv;
	
	// if image hashes match, this is an exact match, no need to comapre
	if (sii->ihash == srch->srchImage->crc)
		cv = 0;
	else // otherwise calc closeness
		cv = iutil_compare(sii->tmb, srch->srchImage->thumb);

	// only consider close'ish files, pointless saving others 
	//if (cv < SEARCH_CLOSENESS_THRESHOLD)
	{
		// Make a search result and add to list
		ImageSearchResult* iss = malloc(sizeof(ImageSearchResult));
		if (iss)
		{
			iss->closeness = cv;
			iss->i = sii;
			iss->next = srch->results;
			srch->results = iss;
		}
	}

	// continue iteration
	return MAP_OK;
}

// search - searches for the image 'file' in the set 'set'. 
void search(char *set, char *file)
{
	// quick checks

	if (!util_directoryExists(set))
		logger(Fatal, "Set does not exist: %s", set);

	if (!util_fileExists(file))
		logger(Fatal, "File does not exist: %s", file);

	// Searcher is passed to the hash map iterator to allow us to keep results 
	Searcher* srch = malloc(sizeof(Searcher));
	if (!srch)
	{
		logger(Fatal, "Couldnt create a searcher");
		return;
	}
	memset(srch, 0, sizeof(Searcher));


	// get image thumbnail & hash etc and put in searcher
	SplitPath* sp = util_splitPath(file);
	srch->srchImage = iutil_getImageInfo(0, file, sp);
	util_freeSplitPath(sp);

	Timer* t = timer_create();

	timer_start(t);
	// Load the set
	Set *s = set_create();
	if (!set_load(s, set))
		return;
	timer_stop(t);
	double loadTime = timer_getElapsedTimeInMilliSec(t);

	set_printStats(s);

	timer_start(t);
	// Search the images in the set using the hashmap callback, maintain results in liked list of ImageSearchResult	
	hashmap_iterate(s->himage, search_compareImages, srch);
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


