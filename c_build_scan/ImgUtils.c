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
 // ImageUtils.h - image utility stuff
 //

#include "common.h"

ImageInfo* iutil_makeImgInfo()
{
	ImageInfo *ii = malloc(sizeof(ImageInfo));
	if (!ii)
		return NULL;
	ii->thumb = 0;
	ii->bytes = 0;
	ii->size = 0;
	ii->crc = 0;
	ii->dirhash = 0;
	ii->filepart = "";
	return ii;
}

void iutil_freeImageInfo(ImageInfo *ii)
{
	free(ii->thumb);
	free(ii->bytes);
	free(ii);
}

void toStr(ImageInfo* ii, char *buf, int maxlen)
{
	snprintf(buf, maxlen, "ImageInfo{name=%s crc=%u}", ii->filepart, ii->crc);
}


ImageInfo *iutil_getImageInfo(Set *s, const char* rpath)
{
	FIBITMAP* check = 0;
	FIBITMAP* rescaled = 0;
	FIBITMAP* tm = 0;
	FREE_IMAGE_FORMAT fif = 0;
	ImageInfo* ii = NULL;
	char path[MAX_PATH];


	util_standardizePath(path, rpath);

	printf("%s\n", path);

	if (  !util_isImageFile(path) )
		return NULL;

	SplitPath* sp = util_splitPath(path);
	char rp[MAX_PATH];
	rp[0] = 0;
	strcat(rp, sp->drv);
	strcat(rp, sp->dir);

	char* relPath = set_relativeTo(s, rp);
	

	FILE* ifs = fopen(path, "rb");
	fseek(ifs, 0, SEEK_END);
	long len = ftell(ifs);
	fseek(ifs, 0, SEEK_SET);

	ii = iutil_makeImgInfo();
	ii->size = len;
	ii->bytes = malloc(ii->size);
	ii->thumb = malloc(TNSMEM);
	if (!ii->bytes || !ii->thumb)
	{
		logger(Fatal,"ran out of memory");
		return NULL;
	}
	size_t br = 0;
	br = fread(ii->bytes, 1, ii->size, ifs);
	fclose(ifs);
	
	
	FIMEMORY* hmem = FreeImage_OpenMemory((BYTE*)ii->bytes, ii->size);
	fif = FreeImage_GetFileTypeFromMemory(hmem, 0);
	check = FreeImage_LoadFromMemory(fif, hmem, 0);
	//FREE_IMAGE_TYPE t = FreeImage_GetImageType(check);
	FreeImage_FlipVertical(check);
	rescaled = FreeImage_Rescale(check, 16, 16, FILTER_BOX);
	FreeImage_Unload(check);
	tm = FreeImage_ConvertTo24Bits(rescaled);
	FreeImage_Unload(rescaled);

	int bbp = FreeImage_GetBPP(tm);
	int sw = FreeImage_GetPitch(tm);
	int w = FreeImage_GetWidth(tm);

	if (tm)
	{
		util_crc32(relPath, strlen(relPath), &ii->dirhash);
		util_crc32(ii->bytes, ii->size, &ii->crc);

		int wb = 0;
		int tb = 0;

		//logger(Info, "Thumbnail is:-");
		for (int r = 0; r < TNSSIZE; r++)
		{
			//logger(Raw, "Line %d", r);
			int8_t* sl = FreeImage_GetScanLine(tm, r);
			tb = 0;
			char buf[100];
			for (int c = 0; c < TNSSIZE; c++)
			{
				//snprintf(buf, 100, "%d %d %d %d %d,", r, c, sl[tb + 0], sl[tb + 1], sl[tb + 2]);
				//logger(Info, "%s", buf);
				ii->thumb[wb++] = sl[tb + 2];
				ii->thumb[wb++] = sl[tb + 1];
				ii->thumb[wb++] = sl[tb + 0];
				tb += 3;
			}
			//logger(Raw, "\n");
		}
		FreeImage_Unload(tm);
	}
	else
	{
		iutil_freeImageInfo(ii);
		ii = 0;
	}

	util_freeSplitPath(sp);
	FreeImage_CloseMemory(hmem);
	
	return ii;

}
