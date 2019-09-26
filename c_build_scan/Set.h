#pragma once


typedef struct _Set
{
	PATH			top;
	SetItemDir		*dirs;
	SetItemFile		*files;
	SetItemImage	*images;
}Set;



typedef struct _ImageSearchResult
{
	SetItemImage* i;
	double closeness;
}ImageSearchResult;

typedef struct _ImageInfo
{
	int32_t size;		// file size
	int8_t* thumb;		// its thumbnail
	int8_t* bytes;		// file bytes
	uint32_t crc;		// crc of bytes
	PATH de;			// path of file
	uint32_t dirhash;	// hash to dir
	char* filepart;		// file name
}ImageInfo;

Set			*set_create();
SetItemDir	*set_addDir(Set *s, PATH path, uint32_t dhash);
SetItemFile* set_addFile(Set* s, uint32_t dhash, const char* name);
SetItemImage* set_addImage(Set* s, ImageInfo *ii);
void		set_setTop(Set *s, PATH _top);
PATH		set_relativeTo(Set *set, PATH dir); // make path relevant to top with a leading /
PATH		set_fullPath(Set *set,  PATH rel);	// make path full by prepending top
void		set_dump(Set* set);				    // print it out
void		set_save(Set* set, PATH dir);       // print it out


