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
 // utils.h - general utility stuff
 //


typedef struct _SplitPath
{
	char* drv;
	char* dir;
	char* file;
	char* ext;
	char* fullfile;
}SplitPath;


void util_standardizePath(char * out, const char *tocheck); // convert \\ to /
BOOL util_directoryExists(const char* szPath);
BOOL util_fileExists(const char* szPath);
BOOL util_isImageFile(const char* szPath);
uint32_t util_crc32_for_byte(uint32_t r);
void util_crc32(const void* data, size_t n_bytes, uint32_t* crc);
char *util_getExtension(const char *filename);
char *util_toLower(const char *s);
SplitPath* util_splitPath(const char* p);
void util_freeSplitPath(SplitPath* sp);
SplitPath* util_makeSplitPath();
void util_absPath(char* out, const char* in);
void util_pathInit(char* path);
int util_mkdir(const char* path);

