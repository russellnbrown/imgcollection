
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

#include <string>
#include <list>
#include <stdio.h>


class matchingItem
{
public:
	double			closeness;
	std::list<std::string>	files;
};

#ifdef USEDB 
bool iccreate(std::string hostport, std::string user, std::string pass, std::string dbase, std::string files);
bool ictestdb(std::string hostport, std::string user, std::string pass, std::string dbase);
std::list<matchingItem*> icsearch(std::string hostport, std::string user, std::string pass, std::string dbase, std::string find, std::string algo);
#endif
bool iccreate(std::string set, std::string files);
std::list<matchingItem*> icsearch(std::string set, std::string find, std::string algo);

