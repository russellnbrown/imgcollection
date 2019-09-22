#pragma once

#include <stdarg.h>

void logto(char* fname);
enum LogLevel { Debug, Info, Warn, Error, Fatal };
void oops(const char *message);
void logger(enum LogLevel ll, char *fmt, ...);
void standardizePath(char *nxt, char *dir, int maxLen); // convert \\ to /
