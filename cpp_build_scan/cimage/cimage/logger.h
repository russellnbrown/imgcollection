
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

#pragma once


 // Logger. a really simple logger to write to both screen and a log file. both 
 // outputs can be set to a threshold

class logger
{
public:

	// log level
	enum Level { Debug, Info, Warn, Error, Fatal, Raw };

	// where to log & what level is logged. 
	static void to(string fname, Level file, Level console);

	// mutes to allow only one thread to log at a time to prevent output getting messy
	static mutex slock;

	// the various log routines
	static inline void info(string txt) { write(Info, txt); }
	static inline void error(string txt) { write(Error, txt); }
	static inline void debug(string txt) { write(Debug, txt); }
	static inline void warn(string txt) { write(Warn, txt); }
	static inline void raw(string txt) { write(Raw, txt); }
	static inline void fatal(string txt) { write(Fatal, txt); exit(-1);  }
	static inline void info(std::ostringstream str) { write(Info, str.str()); }
	static void write(Level l, string txt);

private:

	static ofstream lf;
	static Level lFile;
	static Level lConsole;

};

