#pragma once


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