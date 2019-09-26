#pragma once

typedef char* PATH; // paths are allocated enough space to deal with all path ops
void logto(char* fname);
enum LogLevel { Raw, Debug, Info, Warn, Error, Fatal };
void oops(const char *message);
void logger(enum LogLevel ll, char *fmt, ...);

typedef char* PATH; // all paths guaranteed to have MAX_PATH available 
PATH util_getCwd();
void util_standardizePath(PATH out, const char *tocheck); // convert \\ to /
void util_freePath(PATH path); // frees a PATH
PATH util_makePath(); // returns char buffer for MAX_PATH+1
void util_pathAppend(PATH path, const char* toAppend);
PATH util_copyPath(PATH p);
BOOL util_directoryExists(PATH szPath);
BOOL util_fileExists(PATH szPath);
BOOL util_isImageFile(PATH szPath);
uint32_t util_crc32_for_byte(uint32_t r);
void util_crc32(const void* data, size_t n_bytes, uint32_t* crc);