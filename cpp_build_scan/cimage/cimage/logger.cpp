/*
 * Copyright (C) 2018 russell brown
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


#include "cimage.h"


 // Logger. a really simple logger to write to both screen and a log file. both 
 // outputs can be set to a threshold

ofstream logger::lf;
mutex logger::slock;
logger::Level logger::lFile;
logger::Level logger::lConsole;

void logger::to(string fname, logger::Level flevel, logger::Level clevel)
{
	lFile = flevel;
	lConsole = clevel;

	try
	{
		lf.open(fname.c_str(), ios::out | ios::trunc );
	}
	catch (std::exception se)
	{
		cout << "file open failed" << se.what() << endl;
	}
}


void logger::write(Level l, std::string txt)
{
	scoped_lock s(slock);

	time_t now = time(0);
	char cur[50];
#ifdef WIN32
	ctime_s(cur, sizeof(cur), &now);
#else
	ctime_r(&now, cur);
#endif
	cur[strlen(cur) - 6] = 0;

	string data;

	// add level & timestamp unless raw is selected
	if (l != Raw)
	{
		data.append(cur + 10);
		data.append(" ");
		switch (l)
		{
		case Info: data.append("-I- "); break;
		case Error: data.append("-E- "); break;
		case Warn: data.append("-W- "); break;
		case Debug: data.append("-D- "); break;
		case Fatal: data.append("-F- "); break;
		}
		data.append(": ");
	}

	// add the message
	data.append(txt);

	// write to file if one in use
	if ( lf.is_open() && l >= lFile )
		lf << data << endl;

	// write to screen
	if ( l >= lConsole)
		cout << data << endl;
}


