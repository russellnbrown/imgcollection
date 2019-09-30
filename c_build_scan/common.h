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
// common.h - have all includes together in this one file. faster if
// pre compiled headers are available
//

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
#else
#include <windows.h>
#include <direct.h>
#endif

#include <ctype.h>
#include <FreeImage.h>
#include <sys/types.h>
#include <sys/stat.h>


#define TNSSIZE 16
#define TNSMEM 768
#include "hashmap.h"
#include "Logger.h"
#include "utils.h"
#include "SetBuilder.h"
#include "SetSearch.h"
#include "Set.h"
#include "ImgUtils.h"





