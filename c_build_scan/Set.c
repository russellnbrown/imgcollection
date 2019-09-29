
#include "common.h"


Set *set_create()
{
	Set *s = malloc(sizeof(Set));
	if (s)
	{
		s->top = NULL;
		s->dirs = NULL;
		s->files = NULL;
		s->images = NULL;
	}
	return s;
}



SetItemDir *set_addDir(Set *s, const char* cdir, uint32_t crc)
{
	SetItemDir *dir = malloc(sizeof(SetItemDir));
	if (dir)
	{
		dir->next = NULL;
		dir->path = strdup(cdir);
		dir->next = s->dirs;
		dir->dhash = crc;
		s->dirs = dir;
	}
	
	return dir;
}

SetItemFile* set_addFile(Set* s, uint32_t dhash, const char *name)
{
	SetItemFile* fil = malloc(sizeof(SetItemFile));
	if (fil)
	{
		fil->next = NULL;
		fil->dhash = dhash;
		fil->name = strdup(name);
		fil->next = s->files;
		s->files = fil;
	}

	return fil;
}

SetItemImage* set_addImage(Set* s, ImageInfo* ii)
{
	SetItemImage* fil = malloc(sizeof(SetItemImage));
	if (fil)
	{
		fil->next = NULL;
		fil->ihash = ii->crc;
		fil->tmb = malloc(TNSMEM);
		if ( fil->tmb )
			memcpy(fil->tmb, ii->thumb, TNSMEM);
		fil->next = s->images;
		s->images = fil;
	}

	return fil;
}


void set_setTop(Set *s, const char* _top) 
{   
	logger(Info, "Set, setting top to _top");
	s->top = strdup(_top);
}


char *set_relativeTo(Set *set, const char* dir)
{
	char out[MAX_PATH];
	out[0] = 0;

	if (strncmp(set->top, dir, strlen(set->top) ) == 0) // make sure we can be made relative
	{
		char *bit = (char*)dir + strlen(set->top);
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

	if (strlen(out) > 1 && out[strlen(out) - 1] == '/') // remove any tailing '/'
		out[strlen(out) - 1] = 0;

	return strdup(out);
}

char* set_fullPath(Set *set,  const char* rel)
{
	char out[MAX_PATH];
	out[0] = 0;

	if ( rel[0] == '/' )
		sprintf(out, "%s%s", set->top, rel);
	else
		sprintf(out, "%s/%s", set->top, rel);

	
	return strdup(out);
}

void set_dump(Set* s)
{
	logger(Info, "Top :- %s", s->top);
	logger(Info, "Dirs :-");
	for (SetItemDir* d = s->dirs; d != NULL; d = d->next)
	{
		logger(Info, "\t%s is %u", d->path, d->dhash);
	}
	logger(Info, "Files :-");
	for (SetItemFile* f = s->files; f != NULL; f = f->next)
	{
		logger(Info, "\t%s in %u", f->name, f->dhash);
	}
	logger(Info, "Images :-");
	for (SetItemImage* f = s->images; f != NULL; f = f->next)
	{
		logger(Info, "\t%u", f->ihash);
	}

}

void set_save(Set* s, const char* path)
{
	mkdir(path, 0777);
	char dirfile[MAX_PATH];
	char filefile[MAX_PATH];
	char imgfile[MAX_PATH];
	dirfile[0] = 0;
	filefile[0] = 0;
	imgfile[0] = 0;

	logger(Info, "Saving to path %s", path);

	sprintf(dirfile, "%s/%s", path, "dirs.txt");
	sprintf(filefile, "%s/%s", path, "files.txt");
	sprintf(imgfile, "%s/%s", path, "images.bin");

	FILE* df = fopen(dirfile, "w");
	FILE* ff = fopen(filefile, "w");
	FILE* imf = fopen(imgfile, "wb");

	fprintf(df,"%s\n", s->top);
	for (SetItemDir* d = s->dirs; d != NULL; d = d->next)
	{
		fprintf(df, "%u,%s\n", d->dhash, d->path);
	}
	for (SetItemFile* f = s->files; f != NULL; f = f->next)
	{
		fprintf(ff, "%u,%u,%s\n", f->dhash, 0, f->name);
	}
	for (SetItemImage* f = s->images; f != NULL; f = f->next)
	{
		int64_t crc = f->ihash;
		fwrite(&crc, 8, 1, imf);
		fwrite(f->tmb, TNSMEM, 1, imf);
	}

	fclose(df);
	fclose(ff);
	fclose(imf);


}