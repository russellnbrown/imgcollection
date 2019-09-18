#pragma once


typedef struct _Set
{
	char			*top;
	SetItemDir		*dirs;
	SetItemFile		*files;
	SetItemImage	*images;
}Set;

Set			*createSet();
SetItemDir	*addDir(Set *s, char *path, BOOL rel);
void		setTop(Set *s, char *_top);
void		relativeTo(Set *set, char *out, char *dir);		// make path relevant to top with a leading /
void		fullPath(Set *set, char *out, char *rel);	// make path full by prepending top


