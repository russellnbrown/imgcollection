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
	vsnprintf(emsg,sizeof(emsg), fmt, argp);

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
 
  if ( ll == Fatal )
  {
    exit(-1);
  }

}

