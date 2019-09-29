
#include "common.h"

Set* s = NULL;

int processImageFile(const char* ipath)
{
	SplitPath* sp = util_splitPath(ipath);

	uint32_t dhash = 0;
	util_crc32(sp->fullfile, strlen(sp->fullfile), &dhash);
	SetItemFile* f = set_addFile(s, dhash, sp->fullfile);
	ImageInfo* ii = iutil_getImageInfo(s, ipath);
	if (ii)
	{
		SetItemImage* sii = set_addImage(s, ii);
	}

	util_freeSplitPath(sp);

	return 0;
}

int processDirectory(const char* dpath)
{
	char* rel = set_relativeTo(s, dpath);
	uint32_t dhash = 0;
	util_crc32(rel, strlen(rel), &dhash);
	SetItemDir* d = set_addDir(s, rel, dhash);
	free(rel);
	return 0;
}


#ifdef WIN32

int winscan(const char* thisdir)
{
	WIN32_FIND_DATA ff;

	processDirectory(thisdir);

	char scandir[MAX_PATH];
	scandir[0] = 0;
	
	
	strcat(scandir, thisdir);
	strcat(scandir, "/*");

	logger(Info, "Scan(1) dir=%s scan=%s", thisdir, scandir);
	//logger(Info, "Scan(2) topd %s\n", topd);

	HANDLE fh = FindFirstFileA(scandir, &ff);
	int next = -1;

	if (fh == INVALID_HANDLE_VALUE)
	{
		logger(Fatal, "Can't start FindFirstFile");
		return -1;
	}

	do
	{
		char thisfile[MAX_PATH];
		thisfile[0] = 0;
		snprintf(thisfile, MAX_PATH, "%s/%s", thisdir, ff.cFileName);

		if (ff.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			if (strcmp(ff.cFileName, ".") != 0 && strcmp(ff.cFileName, "..") != 0)
			{
				logger(Info, "Scan(5) isDir, move up %s", thisfile);
				winscan(thisfile);
			}
		}
		else
		{
			processImageFile(thisfile);
		}

		next = FindNextFileA(fh, &ff);
	} while (next != 0);
	FindClose(fh);

	return 0;
}

#else


#define _XOPEN_SOURCE 700
#define __USE_XOPEN_EXTENDED
#include <ftw.h>


int print_entry(const char *item, const struct stat *info, const int typeflag, struct FTW *pathinfo)
{
  PATH filepath = util_makePath();
  strncpy(filepath, item, MAX_PATH);
  
    if (typeflag == FTW_F)
    {
		if (util_isImageFile(filepath))
		{
			processImageFile(filepath);
			logger(Info, "IMAGEFILE %s", filepath);
		}
        else        
			logger(Info, "FILE %s", filepath);
    }
    else if (typeflag == FTW_D || typeflag == FTW_DP)
    {
		processDirectory(filepath);
        logger(Info, "DIR %s", filepath);
    }

  util_freePath(filepath);
    return 0;
}

#endif

void create(char *set, char *dir)
{
	char apath[MAX_PATH];
	util_absPath(apath, dir);
	
	logger(Info, "Path given is %s, Set is %s", dir, set);

  if ( !util_directoryExists(dir) )
  {
    logger(Fatal, "Dir dosnt exist %s ( resolved to %s)", dir, apath);
  }
        
  char pwd[MAX_PATH];
  util_standardizePath(pwd, apath);
  
  logger(Info, "Path to search is %s", pwd);
  
  if ( !util_fileExists(set) )
  {
    logger(Info, "Set dir dosn't exist, making %s", set);
    mkdir(set, 0777);
  }

  s = set_create();
  set_setTop(s, pwd);
  
    logger(Info, "Starting scan at %s", pwd);
      
#ifdef WIN32
	int result = winscan(pwd);
#else
   int result = nftw(pwd, print_entry, 20, FTW_PHYS);
#endif

    if (result != 0)
        logger(Fatal, "Scan failed %d", result);
              
}




	//for (DINFO *d = dirs; d != 0; d = d->next)
	//	printf("%s\n", d->path);




