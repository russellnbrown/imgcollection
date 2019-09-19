
#include "common.h"

int scan(Set *s, SetItemDir *dir)
{
	WIN32_FIND_DATA ff;

	logger(Info,"Scan(1) \"%s\" \"%s\" \n", dir->path, s->top);

	char topd[MAX_PATH];
	topd[0] = 0;
	fullPath(s, topd, dir->path);

	logger(Info,"Scan(1a) \"%s\" ", topd);
	strcat(topd, "*");
	logger(Info, "Scan(2) topd %s\n", topd);

	HANDLE fh = FindFirstFileA(topd, &ff);
	int next = -1;

	if (fh == INVALID_HANDLE_VALUE)
	{
		oops("FindFirstFile");
		return -1;
	}

	do
	{
		logger(Info, "Scan(3) file %s\n", ff.cFileName);

		if (ff.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
		{
			if (strcmp(ff.cFileName, ".") != 0 && strcmp(ff.cFileName, "..") != 0)
			{
				fullPath(s, topd, ff.cFileName);
				logger(Info, "Scan(5) isDir, move up %s\n", topd);
				SetItemDir *d = addDir(s, (char*)topd,TRUE);
				scan(s, d);
			}
		}
		else
		{
			logger(Info, "Scan(5) isFile %s\n", ff.cFileName);
		}

		next = FindNextFileA(fh, &ff);
	} while (next != 0);
	FindClose(fh);
	return 0;

}


void create(char *set, char *dir)
{

	Set *s = createSet();

	logger(Info,"SetBuilder, set is %s, dirs are %s\n", set, dir);
	setTop(s, dir);
	SetItemDir *d = addDir(s, "C:\\TestEnvironments\\scan\\files",TRUE);
	scan(s,d);
}




	//for (DINFO *d = dirs; d != 0; d = d->next)
	//	printf("%s\n", d->path);




