
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


ImageInfo *iutil_getImageInfo(Set *s, PATH path)
{

	char drvpart[_MAX_DRIVE];
	char dirpart[MAX_PATH];
	char fnamepart[_MAX_FNAME];
	char extpart[_MAX_EXT];
	FIBITMAP* check = 0;
	FIBITMAP* rescaled = 0;
	FIBITMAP* tm = 0;
	FREE_IMAGE_FORMAT fif = 0;
	ImageInfo* ii = NULL;


	util_standardizePath(path);

	printf("%s\n", path);

	if (  !util_isImageFile(path) )
		return NULL;


	_splitpath_s(path, drvpart, _MAX_DRIVE, dirpart, MAX_PATH, fnamepart, _MAX_FNAME, extpart, _MAX_EXT);
	PATH rp = util_makePath();
	strcat(rp, drvpart);
	strcat(rp, dirpart);

	PATH relPath = set_relativeTo(s, rp);
	

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
		oops("ran out of memory");
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

	FreeImage_Unload(tm);


	/*
	bool ok = fi.flipVertical();
		ok = fi.rescale(TNSIZE, TNSIZE, FILTER_BOX);
		ok = fi.convertTo24Bits();
		int bpp = fi.getBitsPerPixel();
		int sw = fi.getScanWidth();
		int pw = fi.getWidth();
	*/

	return NULL;

	if (check)
		rescaled = FreeImage_Rescale(check, 16, 16, FILTER_BOX);
	if (rescaled)
	{
		FreeImage_FlipVertical(rescaled);
//		FreeImage_Unload(check);
		tm = FreeImage_ConvertTo24Bits(rescaled);
	//	FreeImage_Unload(rescaled);
	}

	if (tm)
	{
		util_crc32(relPath, strlen(relPath), &ii->dirhash);
		util_crc32(ii->bytes, ii->size, &ii->crc);

		int wb = 0;
		int tb = 0;

		logger(Info, "Thumbnail is:-");
		for (int r = 0; r < TNSSIZE; r++)
		{
			logger(Raw, "Line %d", r);
			int8_t* sl = FreeImage_GetScanLine(tm, r);
			tb = 0;
			for (int c = 0; c < TNSSIZE; c++)
			{
				logger(Raw, "(%u %u %u)", sl[tb + 2]&0xff, sl[tb + 2]&0xff, sl[tb + 2]&0xff);
				ii->thumb[wb++] = sl[tb + 2];
				ii->thumb[wb++] = sl[tb + 1];
				ii->thumb[wb++] = sl[tb + 0];
				tb += 3;
			}
			logger(Raw, "\n");
		}
		FreeImage_Unload(tm);
	}
	else
	{
		iutil_freeImageInfo(ii);
		ii = 0;
	}

	util_freePath(relPath);
	util_freePath(rp);

	// always close the memory stream
	if ( hmem )
		FreeImage_CloseMemory(hmem);

	
	
	return ii;

}
