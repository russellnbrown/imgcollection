
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
	}

	char emsg[1024];
	_vsnprintf(emsg,sizeof(emsg), fmt, argp);
	printf("%s : %s", lvl, emsg);

	if (logfile)
	{
		fprintf(logfile, "%s : %s", lvl, emsg);
		fflush(logfile);
	}
	va_end(argp);

}

void oops(const char *message)
{
	char *buf;
	DWORD error;

	buf = NULL;
	error = GetLastError();
	FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL, error, 0, (LPSTR)&buf, 0, NULL);

	if (buf)
	{
		fprintf(stderr, "%s: %s", message, buf);
		LocalFree(buf);
	}
	else
	{
		/* FormatMessageW failed. */
		fprintf(stderr, "%s: unknown error 0x%x\n", message, error);
	}
}


void standardizePath(char *nxt, char *dir)
{
	char *pstart = dir;
	char *ppos = strstr(dir, "\\");
	*nxt = 0;
	while (ppos)
	{
		strncat(nxt, pstart, ppos - pstart);
		strncat(nxt, "/", 1);
		pstart = ppos + 1;
		ppos = strstr(pstart, "\\");
	}
	strcat(nxt, pstart);
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

