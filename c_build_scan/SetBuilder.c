
#include "common.h"

int scan(Set *s, PATH thisdir)
{
	WIN32_FIND_DATA ff;

	PATH rel = set_relativeTo(s,thisdir);
	uint32_t dhash = 0;
	util_crc32(rel, strlen(rel), &dhash);
	SetItemDir* d = set_addDir(s, rel, dhash);

	PATH scandir = util_copyPath(thisdir);
	util_pathAppend(scandir, "/*");

	logger(Info, "Scan(1) dir=%s scan=%s", thisdir, scandir);
	//logger(Info, "Scan(2) topd %s\n", topd);

	HANDLE fh = FindFirstFileA(scandir, &ff);
	int next = -1;

	if (fh == INVALID_HANDLE_VALUE)
	{
		oops("FindFirstFile");
		return -1;
	}

	do
	{
		//logger(Info, "Scan(3) file %s\n", ff.cFileName);
		PATH thisfile = util_makePath();
		if (thisfile)
			snprintf(thisfile, MAX_PATH, "%s/%s", thisdir, ff.cFileName);

		if (ff.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			if (strcmp(ff.cFileName, ".") != 0 && strcmp(ff.cFileName, "..") != 0)
			{
				logger(Info, "Scan(5) isDir, move up %s", thisfile);
				scan(s, thisfile);
			}
		}
		else
		{
			SetItemFile* f = set_addFile(s, dhash, ff.cFileName);
			ImageInfo* ii = iutil_getImageInfo(s, thisfile);
			if (ii)
			{
				SetItemImage* sii = set_addImage(s, ii);
			}
		}
		util_freePath(thisfile);
		next = FindNextFileA(fh, &ff);
	} while (next != 0);
	FindClose(fh);
	util_freePath(rel);
	util_freePath(scandir);
	return 0;

}

int scanned(const char *fpath, const struct stat *sb, int typeflag)
{
}
void create(char *set, char *dir)
{

	PATH pwd = util_getCwd();
	util_pathAppend(pwd, "/");
	util_pathAppend(pwd, dir);
	util_standardizePath(pwd);

	Set* s = set_create();
	set_setTop(s, pwd);
	

	logger(Info,"SetBuilder, set is %s, dirs are %s", set, dir);
	
	// add the top dir to the set & then scan recursivly from there
	SetItemDir *d = set_addDir(s, dir,TRUE);
  ftw(spwd, scanned, 10);
//	scan(s,d);
}




	//for (DINFO *d = dirs; d != 0; d = d->next)
	//	printf("%s\n", d->path);




