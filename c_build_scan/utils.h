#pragma once

#include <stdarg.h>

enum LogLevel { Debug, Info, Warn, Error, Fatal };
void oops(const char *message);
void logger(enum LogLevel ll, char *fmt, ...);
void standardizePath(char *nxt, char *dir); // convert \\ to /
