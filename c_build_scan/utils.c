
#include "common.h"

static FILE *logfile = NULL;

void logto(char *fname)
{
	logfile = fopen(fname, "w");
}



void logger(enum LogLevel ll, char *fmt, ...)
{
	va_list argp;
	va_start(argp, fmt);


	char *lvl=0;
	switch (ll)
	{
	case Debug: lvl = "-D-"; break;
	case Info: lvl = "-I-"; break;
	case Warn: lvl = "-W-"; break;
	case Error: lvl = "-E-"; break;
	case Fatal: lvl = "-F-"; break;
	case Raw: lvl = ""; break;
	}

	char emsg[1024];
	_vsnprintf(emsg,sizeof(emsg), fmt, argp);

	if ( ll == Raw )
		printf("%s" , emsg);
	else
		printf("%s : %s\n", lvl, emsg);

	if (logfile)
	{
		if ( ll == Raw )
			fprintf(logfile, "%s", emsg);
		else
			fprintf(logfile, "%s : %s\n", lvl, emsg);
		fflush(logfile);
	}
	va_end(argp);

}

void oops(const char *message)
{
	static char buf[1000]; // reserve space incase we ran out of it

  snprintf(buf, 1000, "%s, errno=%d", message, errno);
  printf("%s\n", buf); 
  exit(-1);
}

PATH makepath()
{
  return malloc(MAX_PATH);
}

void freepath(PATH p)
{
  free(p);
}

void util_standardizePath(PATH out, const char *tocheck)
{
	// check enough space for return, simple test as
	// nothing we will do will increase its length
	if (strlen(tocheck) > MAX_PATH)
		oops("Not enough space for standardizePath");

	char *pstart = (char*)tocheck;
	char *ppos = strstr(tocheck, "\\");
	*out = 0;
	while (ppos)
	{
		strncat(out, pstart, ppos - pstart);
		strncat(out, "/", 1);
		pstart = ppos + 1;
		ppos = strstr(pstart, "\\");
	}
	strcat(out, pstart);
	int last = strlen(out)-1;
	if (out[last] == '/')
		out[last] = 0;
}

PATH util_getcd()
{
	char *pwd = makepath();
	getcwd(pwd,MAX_PATH);
	return pwd;
}

void util_pathAppend(PATH path, const char* toAppend)
{
	strcat_s(path, MAX_PATH, toAppend);
}

PATH util_copyPath(PATH p)
{
	PATH out = util_makePath();
	memcpy(out, p, MAX_PATH + 1);
	return out;
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

BOOL util_directoryExists(PATH szPath)
{
	DWORD dwAttrib = GetFileAttributes(szPath);
	return (dwAttrib != INVALID_FILE_ATTRIBUTES && (dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
}

BOOL util_fileExists(PATH path)
{
	struct stat buf;
	int result;
	result = stat(path, &buf);

	return result == 0;
}

BOOL util_isImageFile(PATH path)
{

	char extpart[_MAX_EXT];

	_splitpath_s(path, 0, 0, 0, 0, 0, 0, extpart, _MAX_EXT);
	char* toLower = CharLowerA(extpart);
	if (strcmp(toLower, ".png") == 0)
		return TRUE;
	if (strcmp(toLower, ".fif") == 0)
		return TRUE;
	if (strcmp(toLower, ".jpg") == 0)
		return TRUE;
	if (strcmp(toLower, ".jped") == 0)
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

