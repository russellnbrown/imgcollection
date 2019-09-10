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


// ImgCollectionDirItem holds information about a directory in the set
class ImgCollectionDirItem
{
public:
	ImgCollectionDirItem(int64_t h, string p)
	{
		path = p;
		hash = h;
	}
	int64_t		hash;
	string		path;

	inline string toSave()
	{
		stringstream s;
		s << hash << "," << path;
		return s.str();
	}

	static inline ImgCollectionDirItem *fromSave(string s)
	{
		string dir,hash;
		std::istringstream iss(s);
		std::getline(iss, hash, ',');
		std::getline(iss, dir, ',');
		int64_t dhash = strtoll(hash.c_str(), nullptr, 10);
		ImgCollectionDirItem *d = new ImgCollectionDirItem(dhash, dir);
		return d;
	}


	inline string toStr()
	{
		ostringstream ret;
		ret << "SetItemDir[hash=";
		ret << hex << hash;
		ret << ",path=";
		ret << path;
		ret << "]";
		return  ret.str();
	}


};


