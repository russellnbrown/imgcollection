
#include "common.h"


Set *set_create()
{
	Set *s = malloc(sizeof(Set));
	s->dirs = NULL;
	s->files = NULL;
	s->images = NULL;
	return s;
}



SetItemDir *set_addDir(Set *s, char *cdir, BOOL rel)
{
	SetItemDir *dir = malloc(sizeof(SetItemDir));
	char std[MAX_PATH];
	char stdt[MAX_PATH];

	standardizePath(std, cdir, MAX_PATH);
	if (!rel)
	{
		set_relativeTo(s, stdt, std);
		dir->path = _strdup(stdt);
	}
	else
		dir->path = _strdup(std);
	dir->next = dir;
	s->dirs = dir;
	return dir;
}

void set_setTop(Set *s, char *_top) 
{ 

	s->top = _strdup(_top);

}


void set_relativeTo(Set *set, char *out, char *dir)
{
	out[0] = 0;
	if (strcmp(set->top, dir) == 0) // make sure we can be made relative
	{
		char *bit = dir + strlen(set->top);
		if (*bit == '/')
			strcpy(out, bit);
		else
		{
			strcpy(out, "/");
			strcat(out, bit);
		}
	}
	else
		logger(Fatal, "Dir %s is not under top %s", dir, set->top);
}

void set_fullPath(Set *set, char *out, char *rel)
{
	if ( rel[0] == '/' )
		sprintf(out, "%s%s", set->top, rel);
	else
		sprintf(out, "%s/%s", set->top, rel);
}
