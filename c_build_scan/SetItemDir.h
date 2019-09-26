#pragma once


typedef struct _SetItemDir
{
	PATH path;
	uint32_t dhash;
	struct _SetItemDir *next;
}SetItemDir;


