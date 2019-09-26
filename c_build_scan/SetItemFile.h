#pragma once


typedef struct _SetItemFile
{
	char	*name;
	uint32_t dhash;
	struct  _SetItemFile *next;
}SetItemFile;

