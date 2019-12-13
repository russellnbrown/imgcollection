
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

#pragma once


 // this is the only file included in cpp files. 
 // all headers included in this file
 // used by pch in windows


#include <algorithm>
#include <list>
#include <map>
#include <vector>
#include <stack>
#include <optional>
#include <array>
#include <numeric>
#include <cstring>
#include <mutex>
#include <thread>
#include <iostream>
#include <ctime>
#include <fstream>
#include <sstream>
#include <filesystem>
#include <cstdlib>
#include <cstdint>

#ifndef _WIN32
#include <unistd.h>
#include <stdint.h>
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

typedef int64_t HKey;


// millisecond snooze
#define MSNOOZE(x) std::this_thread::sleep_for(std::chrono::milliseconds(x))

typedef vector<int8_t> ThumbVec;

#include "logger.h"
#include "timer.h"
#include "cmdoptions.h"
#include "utils.h"
#include "stats.h"
#include "collection.h"
#include "builderthreadinfo.h"
#include "builder.h"
#include "search.h"
#include "loader.h"
