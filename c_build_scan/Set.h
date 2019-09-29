#pragma once



typedef struct _Set
{
	char*			top;
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
	char *de;			// path of file
	uint32_t dirhash;	// hash to dir
	char* filepart;		// file name
}ImageInfo;

Set			*set_create();
SetItemDir	*set_addDir(Set *s, const char* path, uint32_t dhash);
SetItemFile* set_addFile(Set* s, uint32_t dhash, const char* name);
SetItemImage* set_addImage(Set* s, ImageInfo *ii);
void		set_setTop(Set *s, const char* _top);
char*		set_relativeTo(Set *set, const char* dir); // make path relevant to top with a leading /
char*		set_fullPath(Set *set, const char* rel);	// make path full by prepending top
void		set_dump(Set* set);				    // print it out
void		set_save(Set* set, const char* dir);       // print it out


