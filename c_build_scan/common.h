#pragma once
#define _CRT_NONSTDC_NO_DEPRECATE
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <inttypes.h>
#ifndef WIN32
#define MAX_PATH 1000
#include <unistd.h>
#include <dirent.h>
#else
#include <windows.h>
#endif

#include <ctype.h>
#include <FreeImage.h>
#include <sys/types.h>
#include <sys/stat.h>



#define TNSSIZE 16
#define TNSMEM 768
#include "Logger.h"
#include "utils.h"
#include "SetBuilder.h"
#include "SetSearch.h"
#include "SetItemDir.h"
#include "SetItemFile.h"
#include "SetItemImage.h"
#include "Set.h"
#include "ImgUtils.h"





