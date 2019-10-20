
/*
 * Copyright (C) 2018 russell brown
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

#pragma once


// this is the only file included in cpp files. 
// all headers inclided in this file
// used by pch in windows

#include <stdio.h>

//#include <windows.h>
#include <string>
#include <algorithm>
//#include <execution>
#include <list>
#include <map>
#include <vector>
#include <stack>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cstdlib>
#include <ctime>
#include <filesystem>
#include <cstring>
#include <mutex>
#include <thread>
#ifndef WIN32
#include <unistd.h>
#endif

#include "FreeImagePlus.h"

using namespace std;
namespace fs = std::filesystem;

// define size of the thumbnails & bytes used
#define TNSIZE 16
#define TNMEM (3*TNSIZE*TNSIZE)
#ifndef MAX_PATH
#define MAX_PATH 2048
#endif

// millisecond snooze
#define MSNOOZE(x) std::this_thread::sleep_for(std::chrono::milliseconds(x))

#include "logger.h"
#include "ImgUtils.h"
#include "ImgCollectionDirItem.h"
#include "ImgCollectionFileItem.h"
#include "ImgCollectionImageItem.h"
#include "Stats.h"
#include "Timer.h"
#include "RunThreadInfo.h"
#include "ImgCollection.h"
#include "ImgCollectionBuilder.h"
#include "ImgCollectionSearch.h"

