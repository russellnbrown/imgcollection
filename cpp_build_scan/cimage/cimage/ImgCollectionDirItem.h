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


//
// ImgCollectionDirItem 
//
// Holds information about a directory in the set and a couple of helper functions
//
class ImgCollectionDirItem
{
public:
	ImgCollectionDirItem(int64_t h, string p, time_t lastMod)
	{
		path = p;
		hash = h;
		lmod = lastMod;
	}
	int64_t				hash;		// csc32 of directory path ( used as indef ny FileItem )
	string				path;		// the path ( relative to collection top )
	time_t				lmod;// time directory was last modified

	// toSave - create a string suitable for saving to a dir file
	inline string toSave()
	{
		stringstream s;
		s << hash << "|" << path << "|" << lmod;
		return s.str();
	}

	// return a DirItem by parsing a line from a dir file
	static inline ImgCollectionDirItem *fromSave(string s)
	{
		string dir,hash,ltime;
		std::istringstream iss(s);
		std::getline(iss, hash, '|');
		std::getline(iss, dir, '|');
		std::getline(iss, ltime, '|');
		int64_t dhash = strtoll(hash.c_str(), nullptr, 10);
		time_t tt = strtoll(ltime.c_str(), nullptr, 10);
		ImgCollectionDirItem *d = new ImgCollectionDirItem(dhash, dir, tt);
		return d;
	}

	// toStr 
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


