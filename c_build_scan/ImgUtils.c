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
 // ImgUtils.c : utility menthods related to image processing
 //

#include "common.h"

//
// iutil_makeImgInfo - allocates & initializes memory for an ImageInfo struct
//
ImageInfo* iutil_makeImgInfo()
{
	ImageInfo* ii = malloc(sizeof(ImageInfo));
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

//
// iutil_freeImageInfo - free memory used ImageInfo struct including allocated arrays
//
void iutil_freeImageInfo(ImageInfo* ii)
{
	free(ii->thumb);
	free(ii->bytes);
	free(ii);
}

//
// toStr - provides test string of contents
//
void toStr(ImageInfo* ii, char* buf, int maxlen)
{
	snprintf(buf, maxlen, "ImageInfo{name=%s crc=%u}", ii->filepart, ii->crc);
}

//
// iutil_getImageInfo - gets information about an image ( including thumbnail ) and
// return an ImageInfo structure 
//
ImageInfo* iutil_getImageInfo(Set* s, const char* rpath)
{
	FIBITMAP* check = 0;
	FIBITMAP* rescaled = 0;
	FIBITMAP* tm = 0;
	FREE_IMAGE_FORMAT fif = 0;
	ImageInfo* ii = NULL;
	char path[MAX_PATH];

	// make sure path looks standard
	util_standardizePath(path, rpath);

	// check it is an image file, else quit
	if (!util_isImageFile(path))
		return NULL;

	// split path into constituent parts
	SplitPath* sp = util_splitPath(path);
	char rp[MAX_PATH];
	util_pathInit(rp);
	strcat(rp, sp->drv);
	strcat(rp, sp->dir);
	util_freeSplitPath(sp);

	// get the path relative to set top
	char relPath[MAX_PATH];
	util_pathInit(relPath);
	if (s)
		set_relativeTo(s, rp, relPath);
	else
		strncpy(relPath, rp, MAX_PATH);

	// open the image file
	FILE* ifs = fopen(path, "rb");

	// find out its size 
	fseek(ifs, 0, SEEK_END);
	long len = ftell(ifs);
	fseek(ifs, 0, SEEK_SET);

	// create ImageInfo structure and allocate space for thumbnail & files bytes
	ii = iutil_makeImgInfo();
	ii->size = len;
	ii->bytes = malloc(ii->size);
	ii->thumb = malloc(TNSMEM);
	if (!ii->bytes || !ii->thumb)
	{
		logger(Fatal, "ran out of memory");
	}

	// read file bytes into imageinfo and close file
	size_t br = 0;
	br = fread(ii->bytes, 1, ii->size, ifs);
	fclose(ifs);

	// now get the files thumbnail, start by opening the image from a freeimage
	// memory buffer using the file bytes we have in imageinfo rather than the 
	// file itself
	FIMEMORY* hmem = FreeImage_OpenMemory((BYTE*)ii->bytes, ii->size);
	fif = FreeImage_GetFileTypeFromMemory(hmem, 0);

	// read image
	check = FreeImage_LoadFromMemory(fif, hmem, 0);

	// to ensure thumbnail is consistent with other languages and is stored
	// top down, we need to flip it
	FreeImage_FlipVertical(check);

	// resize to thumb size, free original images and make sure it it 24 bits (RGB) 
	// per pixel
	rescaled = FreeImage_Rescale(check, 16, 16, FILTER_BOX);
	FreeImage_Unload(check);
	tm = FreeImage_ConvertTo24Bits(rescaled);
	FreeImage_Unload(rescaled);

	// make sure it is as expected
	int bbp = FreeImage_GetBPP(tm);
	int sw = FreeImage_GetPitch(tm);
	int w = FreeImage_GetWidth(tm);
	if (tm && bbp == 24 || sw == (TNSSIZE * 3) || w == TNSSIZE)
	{
		// get hashes for directory path & files bytes
		util_crc32(relPath, strlen(relPath), &ii->dirhash);
		util_crc32(ii->bytes, ii->size, &ii->crc);

		// now read the scanlins and store pixels in imageinfo thumbnail structure
		int wb = 0;
		int tb = 0;
		for (int r = 0; r < TNSSIZE; r++)
		{
			int8_t* sl = FreeImage_GetScanLine(tm, r);
			tb = 0;
			for (int c = 0; c < TNSSIZE; c++)
			{
				ii->thumb[wb++] = sl[tb + 2];
				ii->thumb[wb++] = sl[tb + 1];
				ii->thumb[wb++] = sl[tb + 0];
				tb += 3;
			}
		}

		// free image
		FreeImage_Unload(tm);
	}
	else
	{
		// failed to read, free image info
		iutil_freeImageInfo(ii);
		ii = 0;
	}

	// free freeimage memory buffer	
	FreeImage_CloseMemory(hmem);

	return ii;

}

// iutil_compare - ultra simple compare of two thumbnails, returns the
// total in RGB comparison
double iutil_compare(uint8_t* i1, uint8_t* i2)
{

	float rd = 0.0;
	float gd = 0.0;
	float bd = 0.0;
	double td = 0.0;

	for (int tix = 0; tix < TNSMEM; tix += 3)
	{
		int srx = i1[tix];
		int crx = i2[tix];
		int sgx = i1[tix + 1];
		int cgx = i2[tix + 1];
		int sbx = i1[tix + 2];
		int cbx = i2[tix + 2];

		int tx = abs(sbx - cbx) + abs(sgx - cgx) + abs(srx - crx);
		td += (double)tx;
	}

	return td;
}
