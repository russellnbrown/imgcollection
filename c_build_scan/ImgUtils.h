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
// ImgUtils methods to get 'ImageInfo' about a file. Included crc's & thumbnail
//


double iutil_compare(uint8_t* t1, uint8_t* t2);
ImageInfo* iutil_makeImgInfo();
ImageInfo* iutil_getImageInfo(Set* s, const char* path, SplitPath *);