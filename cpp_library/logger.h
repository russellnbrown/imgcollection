
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

#pragma once


 // Logger. a really simple logger to write to both screen and a log file. both 
 // outputs can be set to a threshold

class logger
{
public:

	// log level
	enum Level { Debug, Info, Warn, Error, Fatal, Raw };

	// where to log & what level is logged. 
	static void to(std::string fname, Level file, Level console);


	// the various log routines
	static inline void info(std::string txt) { write(Info, txt); }
	static inline void error(std::string txt) { write(Error, txt); }
	static inline void debug(std::string txt) { write(Debug, txt); }
	static inline void warn(std::string txt) { write(Warn, txt); }
	static inline void raw(std::string txt) { write(Raw, txt); }
	static inline void fatal(std::string txt) { write(Fatal, txt); exit(-1);  }
	static void write(Level l, std::string txt);
	static inline void excep(std::string txt, std::exception &e) { std::string s = e.what();  write(Error, txt + ": " + s); }


};

