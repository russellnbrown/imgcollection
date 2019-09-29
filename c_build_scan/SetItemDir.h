#pragma once


typedef struct _SetItemDir
{
	char* path;
	uint32_t dhash;
	struct _SetItemDir *next;
}SetItemDir;


