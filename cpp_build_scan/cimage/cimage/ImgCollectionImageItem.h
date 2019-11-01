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

//
// ImgCollectionImageItem 
//
// holds information about an image in the set
//
class ImgCollectionImageItem
{
public:
	ImgCollectionImageItem(int64_t crc, int8_t *thumb)
	{
		this->crc = crc;
		this->thumb = new int8_t[TNMEM];
		std::memcpy(this->thumb,thumb, TNMEM);
		tix = -1;
		
	}
	int64_t		crc;	// the crc of the image file contents
	int8_t		*thumb; // rgb tuples forming a thumbnail of image
	int			tix;	// see threading notes
	

	string	toStr()
	{
		stringstream ss;
		ss << "SetItemImage[crc=" << crc << "]";
		return ss.str();
	}
};


