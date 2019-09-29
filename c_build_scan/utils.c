
#include "common.h"




void util_standardizePath(char* out, const char* tocheck)
{
	// check enough space for return, simple test as
	// nothing we will do will increase its length
	if (strlen(tocheck) > MAX_PATH)
		logger(Fatal, "Not enough space for standardizePath");

	char* pstart = (char*)tocheck;
	char* ppos = strstr(tocheck, "\\");
	*out = 0;
	while (ppos)
	{
		strncat(out, pstart, ppos - pstart);
		strncat(out, "/", 1);
		pstart = ppos + 1;
		ppos = strstr(pstart, "\\");
	}
	strcat(out, pstart);
	int last = strlen(out) - 1;
	if (out[last] == '/')
		out[last] = 0;
}




/* Simple public domain implementation of the standard CRC32 checksum.
 * Outputs the checksum for each file given as a command line argument.
 * Invalid file names and files that cause errors are silently skipped.
 * The program reads from stdin if it is called with no arguments. */

 // http://home.thep.lu.se/~bjorn/crc/

uint32_t util_crc32_for_byte(uint32_t r) {
	for (int j = 0; j < 8; ++j)
		r = (r & 1 ? 0 : (uint32_t)0xEDB88320L) ^ r >> 1;
	return r ^ (uint32_t)0xFF000000L;
}

void util_crc32(const void* data, size_t n_bytes, uint32_t* crc) {
	static uint32_t table[0x100];
	if (!*table)
		for (size_t i = 0; i < 0x100; ++i)
			table[i] = util_crc32_for_byte(i);
	for (size_t i = 0; i < n_bytes; ++i)
		* crc = table[(uint8_t)* crc ^ ((uint8_t*)data)[i]] ^ *crc >> 8;
}

BOOL util_directoryExists(const char* path)
{
	struct stat buf;
	int result;
	result = stat(path, &buf);
	if (result == 0)
	{
		return buf.st_mode&S_IFDIR;
	}
	return FALSE;
}

BOOL util_fileExists(const char* path)
{
	struct stat buf;
	int result;
	result = stat(path, &buf);
	if (result == 0)
	{
		return buf.st_mode & S_IFREG;
	}
	return FALSE;
}

void util_freeSplitPath(SplitPath* sp)
{
	if ( sp )
	{
		free(sp->dir);
		free(sp->file);
		free(sp->ext);
		free(sp->drv);
		free(sp->fullfile);
	}
	free(sp);
}

SplitPath* util_makeSplitPath()
{
	SplitPath *s = malloc(sizeof(SplitPath));
	if ( s )
		memset(s, 0, sizeof(SplitPath));
	return s;
}


void util_absPath(char* out, const char* in)
{
#ifdef WIN32
	_fullpath(out, in, MAX_PATH);
#else
	realpath(in, out);
#endif
}

SplitPath* util_splitPath(const char* p)
{
	char path[MAX_PATH];
	char drv[MAX_PATH];
	char dir[MAX_PATH];
	char fil[MAX_PATH];
	char ful[MAX_PATH];
	char ext[MAX_PATH];
	memset(path, 0, MAX_PATH);
	memset(drv, 0, MAX_PATH);
	memset(dir, 0, MAX_PATH);
	memset(ful, 0, MAX_PATH);
	memset(fil, 0, MAX_PATH);
	memset(ext, 0, MAX_PATH);

	BOOL isFile = util_fileExists(p);

	char* cpos = strchr(p, ':');
	if (cpos)
	{
		int cidx = (int)(cpos - p);
		strncpy(drv, p, cidx + 1);
		p = cpos + 1;
	}

	if (isFile)
	{
		char* lspos = strrchr(p, '/');
		char* expos = strrchr(p, '.');
		if (lspos && expos)
		{
			int lsidx = (int)(lspos - p);
			int exidx = (int)(expos - p);
			strncpy(dir, p, lsidx);
			strncpy(ful, lspos + 1, strlen(lspos) - 1);
			strncpy(fil, lspos + 1, strlen(lspos) - strlen(expos) -1  );
			strncpy(ext, expos , strlen(expos) );
		}
	}
	else
	{
		strncpy(dir, p, strlen(p));
	}


	SplitPath* sp = util_makeSplitPath();


	if (strlen(ext) > 1)
		sp->ext = strdup(ext + 1);
	sp->file = strdup(fil);
	sp->drv = strdup(drv);
	sp->dir = strdup(dir);
	sp->fullfile = strdup(ful);

	return sp;


}

#ifdef BOBBY
SplitPath* util_splitPath(const char* p)
{
	char drv[_MAX_DRIVE];
	char dir[MAX_PATH];
	char sdir[MAX_PATH];
	char fil[_MAX_FNAME];
	char ext[_MAX_EXT];
	drv[0] = 0;
	dir[0] = 0;
	sdir[0] = 0;
	fil[0] = 0;
	ext[0] = 0;

	SplitPath* sp = util_makeSplitPath();

	_splitpath_s(p, drv, _MAX_DRIVE, dir, _MAX_DIR, fil, _MAX_FNAME, ext, _MAX_EXT);
	util_standardizePath(sdir, dir);

	if ( strlen(ext) > 1 )
		sp->ext = strdup(ext+1);
	sp->file = strdup(fil);
	sp->drv = strdup(drv);
	sp->dir = strdup(dir);
	sp->fullfile = malloc(strlen(fil) + strlen(ext) + 1);
	if(sp->fullfile)
		sprintf(sp->fullfile, "%s%s", fil, ext);
	return sp;
}
#endif

void util_pathInit(char* path)
{
	memset(path, 0, MAX_PATH);
}


char *util_getExtension(const char* filename) // stacko
{
    const char *dot = strrchr(filename, '.');
    
    if(!dot || dot == filename) 
      return NULL;
          
    return strdup(dot + 1);
}

char *util_toLower(const char *s) // stacko
{
    char *d = (char *)malloc(strlen(s)+1);
	memset(d, 0, strlen(s) + 1);
    char *rv = d;
    while (*s)
    {
        *d = tolower(*s);
        d++;
        s++;
    }
	*d = 0;
    return rv;
}

BOOL util_isImageFile(const char* path)
{

	char *extpart = util_getExtension(path);
	char* toLower = util_toLower(extpart);

	if (strcmp(toLower, "png") == 0)
		return TRUE;
	if (strcmp(toLower, "tif") == 0)
		return TRUE;
	if (strcmp(toLower, "jpg") == 0)
		return TRUE;
	if (strcmp(toLower, "gif") == 0)
		return TRUE;
	if (strcmp(toLower, "jpeg") == 0)
		return TRUE;

	return FALSE;
}


//#include <Python.h>


/*
int initPython()
{
Py_SetProgramName("E:\\Python37\\python.exe");
Py_Initialize();
PyRun_SimpleString("from time import time,ctime\n"
"print ('Today is',ctime(time()))\n");
Py_Finalize();

}
*/

