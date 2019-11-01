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
 // Set.h - holds structures that form the 'ImageCollection'
 //

typedef struct _SetItemImage
{
	uint32_t	ihash;
	uint8_t* tmb;
	struct  _SetItemImage* next; // used by search threads, SetItemImages are stored in a hashmap
}SetItemImage;

typedef struct _SetItemDir
{
	char* path;
	uint32_t dhash;
	struct _SetItemDir* next;
}SetItemDir;

typedef struct _SetItemFile
{
	char* name;
	uint32_t dhash;
	uint32_t ihash;
	struct  _SetItemFile* next;
}SetItemFile;


typedef struct _SrchThreadInfo
{
	HANDLE	threadId;
	int		threadNum;
	SetItemImage* images;
}SrchThreadInfo;

typedef struct _Set
{
	char*			top;
	SetItemDir*		dirs;
	SetItemFile*	files;
	map_t			himage;
	int32_t			numImages;
	int32_t			numFiles;
	int32_t			numDirs;
	SrchThreadInfo* threads;
	int				numThreads;
}Set;



typedef struct _ImageSearchResult
{
	SetItemImage* i;
	double closeness;
	struct _ImageSearchResult* next;
}ImageSearchResult;

typedef struct _ImageInfo
{
	int32_t size;		// file size
	int8_t* thumb;		// its thumbnail
	int8_t* bytes;		// file bytes
	uint32_t crc;		// crc of bytes
	char* de;			// path of file
	uint32_t dirhash;	// hash to dir
	char* filepart;		// file name
}ImageInfo;

Set*			set_create();
SetItemDir*		set_addDir(Set* s, const char* path, uint32_t dhash);
SetItemFile*	set_addFile(Set* s, uint32_t dhash, uint32_t ihash, const char* name);
SetItemImage*	set_addImage(Set* s, ImageInfo* ii);
void			set_setTop(Set* s, const char* _top);
void			set_relativeTo(Set* set, const char* dir, char* out);
void			set_fullPath(Set* set, const char* rel, char* out);	
void			set_dump(Set* set);
void			set_save(Set* set, const char* dir);
BOOL			set_load(Set* set, const char* dir);
void			set_printStats(Set* s);

