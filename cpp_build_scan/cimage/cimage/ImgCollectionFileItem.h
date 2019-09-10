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


// SetItemFile holds information about a file in the set
class ImgCollectionFileItem
{
public:
	ImgCollectionFileItem(int64_t dhash, int64_t crc, string name)
	{
		this->dhash = dhash;
		this->crc = crc;
		this->name = name;
	}
	int64_t	dhash;
	int64_t	crc;
	string	name;

	string	toStr()
	{ 
		stringstream ss;
		ss << "SetItemFile[name=" << name << ", crc=" << crc << ", dhash=" << name << "]";
		return ss.str();
	}

	string	toSave()
	{
		stringstream ss;
		ss << dhash << "," << crc << "," << name;
		return ss.str();
	}

	static ImgCollectionFileItem *fromSave(string &s)
	{
		string crc, name, hash;
		std::istringstream iss(s);
		std::getline(iss, hash, ',');
		std::getline(iss, crc, ',');
		std::getline(iss, name, ',');
		int64_t dhash = strtoll(hash.c_str(), nullptr, 10);
		int64_t lcrc = strtoll(crc.c_str(), nullptr, 10);
		ImgCollectionFileItem *f = new ImgCollectionFileItem(dhash, lcrc, name);
		return f;
	}

};

