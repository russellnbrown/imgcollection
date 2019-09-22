#pragma once


typedef struct _Set
{
	char			*top;
	SetItemDir		*dirs;
	SetItemFile		*files;
	SetItemImage	*images;
}Set;

Set			*set_create();
SetItemDir	*set_addDir(Set *s, char *path, BOOL rel);
void		set_setTop(Set *s, char *_top);
void		set_relativeTo(Set *set, char *out, char *dir);		// make path relevant to top with a leading /
void		set_fullPath(Set *set, char *out, char *rel);	// make path full by prepending top


