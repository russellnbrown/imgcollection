
#include "c_build_scan.h"


Set *createSet()
{
	Set *s = malloc(sizeof(Set));
	s->dirs = NULL;
	s->files = NULL;
	s->images = NULL;
	return s;
}



SetItemDir *addDir(Set *s, char *cdir, BOOL rel)
{
	SetItemDir *dir = malloc(sizeof(SetItemDir));
	char std[MAX_PATH];
	char stdt[MAX_PATH];

	standardizePath(std, cdir);
	if (!rel)
	{
		relativeTo(s, stdt, std);
		dir->path = _strdup(stdt);
	}
	else
		dir->path = _strdup(std);
	dir->next = dir;
	s->dirs = dir;
	return dir;
}

void setTop(Set *s, char *_top) 
{ 
	char std[MAX_PATH];
	standardizePath(std,_top);
	s->top = _strdup(std);
	if (s->top[strlen(s->top) - 1] == '/') // make sure to trailing '/'
		s->top[strlen(s->top) - 1] = 0;

}


void relativeTo(Set *set, char *out, char *dir)
{
	out[0] = 0;
	if (strcmp(set->top, dir) == 0) // make sure we can be made relative
	{
		char *bit = dir + strlen(set->top);
		if (bit == '/')
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

void fullPath(Set *set, char *out, char *rel)
{
	if ( rel[0] == '/' )
		sprintf(out, "%s%s", set->top, rel);
	else
		sprintf(out, "%s/%s", set->top, rel);
}
