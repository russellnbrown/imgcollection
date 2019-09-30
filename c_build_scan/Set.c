/*
 * Copyright (C) 2019 russell brown
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 //
 // Set.c : functions relating to Image Collection aka 'Set'  
 //

#include "common.h"

// set_create - create & initialize the set structure.
Set* set_create()
{
	Set* s = malloc(sizeof(Set));
	if (s)
	{
		s->top = NULL;
		s->dirs = NULL;
		s->files = NULL;
		s->numDirs = 0;
		s->numFiles = 0;
		s->numImages = 0;
		s->himage = hashmap_new();
	}
	return s;
}

// set_addDir - add a directory to the set
SetItemDir* set_addDir(Set* s, const char* cdir, uint32_t crc)
{
	// create the directory item
	SetItemDir* dir = malloc(sizeof(SetItemDir));
	if (dir)
	{
		// initialize it with data provided
		dir->next = NULL;
		dir->path = strdup(cdir); // make local copy
		dir->next = s->dirs;
		// maintain a linked list
		dir->dhash = crc;
		s->dirs = dir;
		s->numDirs++;
	}

	return dir;
}

// set_addFile - add a file to the set
SetItemFile* set_addFile(Set* s, uint32_t dhash, uint32_t ihash, const char* name)
{
	// create the file item
	SetItemFile* fil = malloc(sizeof(SetItemFile));
	if (fil)
	{
		// initialize with data provided
		fil->next = NULL;
		fil->dhash = dhash;
		fil->ihash = ihash;
		fil->name = strdup(name); // make local copy
		// maintain a linked list
		fil->next = s->files;
		s->files = fil;
		s->numFiles++;
	}

	return fil;
}

// set_addImage - add an image to the set
SetItemImage* set_addImage(Set* s, ImageInfo* ii)
{
	// create the image item
	SetItemImage* fil = malloc(sizeof(SetItemImage));
	if (fil)
	{
		// initialize with data provided
		fil->next = NULL;
		fil->ihash = ii->crc;
		// copt the thumbnsil locslly
		fil->tmb = malloc(TNSMEM);
		if (fil->tmb)
			memcpy(fil->tmb, ii->thumb, TNSMEM);
		SetItemImage* existing = 0;
		int dup = hashmap_get(s->himage, fil->ihash,(any_t)&existing);
		if (dup == MAP_MISSING)
		{
			hashmap_put(s->himage, fil->ihash, fil);
			s->numImages++;
		}
		else
			logger(Info, "Duplicate %u", fil->ihash);
	}

	return fil;
}

// set_printStats - print out number of things in the set
void set_printStats(Set* s)
{
	logger(Info, "Set: top %s, num images %d, num files %d, num dirs %d", s->top, s->numImages, s->numFiles, s->numDirs);
}

// set_setTop - set the 'top' path, the one all directory entries are relative to
void set_setTop(Set* s, const char* _top)
{
	logger(Info, "Set, setting top to %s", _top);
	s->top = strdup(_top);
}

// set_relativeTo - given a path, make it relative to the set's top path
void set_relativeTo(Set* set, const char* dir, char *out)
{
	util_pathInit(out);

	// given path must match the set's top
	if (strncmp(set->top, dir, strlen(set->top)) == 0) // make sure we can be made relative
	{
		// extract part of path after the 'top' bit
		char* bit = (char*)dir + strlen(set->top);
		// make sure it starts with a '/'
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

	// remove any tailing '/'
	if (strlen(out) > 1 && out[strlen(out) - 1] == '/') // remove any tailing '/'
		out[strlen(out) - 1] = 0;


}

// set_fullPath - return the full path of a path relative to the set top
void set_fullPath(Set* set, const char* rel, char* out)
{
	util_pathInit(out);

	if (rel[0] == '/')
		sprintf(out, "%s%s", set->top, rel);
	else
		sprintf(out, "%s/%s", set->top, rel);


}

int logImage(any_t q1, any_t q2)
{
	SetItemImage* sii = (SetItemImage*)q2;
	logger(Info, "\t%u", sii->ihash);
	return MAP_OK;
}

// set_dump - writes out the contents of the set to logger
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
		logger(Info, "\t%s (%u) in %u", f->name, f->ihash, f->dhash);
	}
	logger(Info, "Images :-");
	hashmap_iterate(s->himage, (PFany)logImage, 0);
	

}


int saveImage(any_t q1, any_t q2)
{
	SetItemImage* f = (SetItemImage*)q2;
	FILE* imf = (FILE*)q1;

	int64_t crc = f->ihash;
	fwrite(&crc, 8, 1, imf);
	fwrite(f->tmb, TNSMEM, 1, imf);


	return MAP_OK;
}

// set_save - save the set to disk. The set is saved as three seperate files under the
// 'path' directory.
void set_save(Set* s, const char* path)
{
	// make the directory to hold the set files

	logger(Info, "Saving to path %s", path);
	util_mkdir(path);

	// create paths for the files
	char dirfile[MAX_PATH];
	char filefile[MAX_PATH];
	char imgfile[MAX_PATH];
	util_pathInit(dirfile);
	util_pathInit(filefile);
	util_pathInit(imgfile);
	sprintf(dirfile, "%s/%s", path, "dirs.txt");
	sprintf(filefile, "%s/%s", path, "files.txt");
	sprintf(imgfile, "%s/%s", path, "images.bin");

	// and open them. the image file is binary as it contains thumbnails in raw format
	FILE* df = fopen(dirfile, "w");
	FILE* ff = fopen(filefile, "w");
	FILE* imf = fopen(imgfile, "wb");

	// wtite to the dirs file. First line is the 'top' directory
	fprintf(df, "%s\n", s->top);
	// further lines are the relative paths & hashes 
	for (SetItemDir* d = s->dirs; d != NULL; d = d->next)
	{
		fprintf(df, "%u,%s\n", d->dhash, d->path);
	}

	// write to the files file. each line has the directory hash of its
	// directory, its image hash, and the file name
	for (SetItemFile* f = s->files; f != NULL; f = f->next)
	{
		fprintf(ff, "%u,%u,%s\n", f->dhash, f->ihash, f->name);
	}

	// write the images. each record is the image hash & thumbnail bytes
	hashmap_iterate(s->himage, (PFany)saveImage, (any_t)imf);

	// close all files
	fclose(df);
	fclose(ff);
	fclose(imf);


}

int phash(any_t q1, any_t q2)
{
	SetItemImage* sii = (SetItemImage*)q2;
	logger(Info, "HashIter, image=%u", sii->ihash);
	return MAP_OK;
}

// set_save - save the set to disk. The set is saved as three seperate files under the
// 'path' directory.
BOOL set_load(Set* s, const char* path)
{
	// create paths for the files
	char dirfile[MAX_PATH];
	char filefile[MAX_PATH];
	char imgfile[MAX_PATH];
	util_pathInit(dirfile);
	util_pathInit(filefile);
	util_pathInit(imgfile);
	sprintf(dirfile, "%s/%s", path, "dirs.txt");
	sprintf(filefile, "%s/%s", path, "files.txt");
	sprintf(imgfile, "%s/%s", path, "images.bin");

	// and open them. the image file is binary as it contains thumbnails in raw format
	FILE* df = fopen(dirfile, "r");
	FILE* ff = fopen(filefile, "r");
	FILE* imf = fopen(imgfile, "rb");

	if (!df || !ff || !imf)
	{
		logger(Fatal, "One or more set files did not open");
		return FALSE;
	}

	// wtite to the dirs file. First line is the 'top' directory
	int ok = 0;
	char top[MAX_PATH];
	util_pathInit(top);

	ok = fscanf(df, "%s\n", top);
	s->top = strdup(top);

	// further lines are the relative paths & hashes 
	char file[MAX_PATH];
	while (!feof(df))
	{
		SetItemDir* d = malloc(sizeof(SetItemDir));
		if (d)
		{
			ok = fscanf(df, "%u,%s\n", &d->dhash, file);
			d->path = strdup(file);
			d->next = s->dirs;
			s->dirs = d;
			s->numDirs++;
		}
	}

	while (!feof(ff))
	{
		SetItemFile* f = malloc(sizeof(SetItemFile));
		if (f)
		{
			ok = fscanf(ff, "%u,%u,%s\n", &f->dhash, &f->ihash, file);
			f->name = strdup(file);
			f->next = s->files;
			s->files = f;
			s->numFiles++;
		}
	}

	while (!feof(imf))
	{
		SetItemImage* ii = malloc(sizeof(SetItemImage));
		if (ii)
		{
			int64_t ihash;
			ii->tmb = malloc(TNSMEM);
			if (ii->tmb)
			{
				ok=fread(&ihash, 8, 1, imf);
				ok=fread(ii->tmb, 1, TNSMEM, imf);
				ii->ihash = (uint32_t)ihash;
				hashmap_put(s->himage, ii->ihash, ii);
				s->numImages++;
			}
		}
	}

	//hashmap_iterate(s->himage, phash, 0);

	logger(Info, "Read set OK");

	// close all files
	fclose(df);
	fclose(ff);
	fclose(imf);

	return TRUE;

}