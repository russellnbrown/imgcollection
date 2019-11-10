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
 // SetSearch.h - methods to search the 'Image Database'
 //

#define SEARCH_CLOSENESS_THRESHOLD 50000

// Searcher is used to hold image we are searching for and the results
typedef struct _SetSearchInfo
{
	ImageSearchResult* results; // used to save search results
	ImageInfo* srchImage;		// the image to search for
	MUTEXHANDLE srchProtect;	// mutex to sync access to search results

}SetSearchInfo;


void search(char* set, char* file, BOOL useThreads);
SetSearchInfo *search_makeSetSearchInfo();
void search_freeSetSearchInfo(SetSearchInfo* si);
void search_resultSwap(ImageSearchResult* p1, ImageSearchResult* p2);
void search_resultSort(ImageSearchResult* head);
SetItemFile* search_findFirstFile(Set* s, uint32_t hash);
SetItemDir* search_findDirectory(Set* s, uint32_t dhash);
void search_addResult(ImageSearchResult* iss);
void search_compareImages(void* q1);
void* searchThreadFunc(void* param);
void run_threads(Set* s, SetSearchInfo* srch);
