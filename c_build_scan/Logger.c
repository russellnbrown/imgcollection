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
 // Logger.c : very simple logging functions
 //


#include "common.h"

// the file we will log to
static FILE* logfile = NULL;

// logto - initializes logging to a file.
void logto(char* fname)
{
	logfile = fopen(fname, "w");
}

// logger - writes message & level to screen and or file. uses varargs.
void logger(enum LogLevel ll, char* fmt, ...)
{
	va_list argp;
	va_start(argp, fmt);

	// level text to add to string
	char* lvl = 0;
	switch (ll)
	{
	case Debug: lvl = "-D-"; break;
	case Info: lvl = "-I-"; break;
	case Warn: lvl = "-W-"; break;
	case Error: lvl = "-E-"; break;
	case Fatal: lvl = "-F-"; break;
	case Raw: lvl = ""; break;
	}

	// create log string 
	char emsg[1024];
	vsnprintf(emsg, sizeof(emsg), fmt, argp);

	// write to screen
	if (ll == Raw)
		printf("%s", emsg);
	else
		printf("%s : %s\n", lvl, emsg);

	// write to file if it was specified in logto
	if (logfile)
	{
		if (ll == Raw)
			fprintf(logfile, "%s", emsg);
		else
			fprintf(logfile, "%s : %s\n", lvl, emsg);
		fflush(logfile);
	}
	va_end(argp);

	// exit if it was a fatal error
	if (ll == Fatal)
	{
		exit(-1);
	}

}

