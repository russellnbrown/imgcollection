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

// holds stats about files scanned 
class Stats
{
public:

	mutex slock;
	inline void incImages() { std::scoped_lock s(slock);  numImages++; }
	inline void incThreads() { std::scoped_lock s(slock); numThreads++; }
	inline void incFiles() { scoped_lock s(slock); numFiles++; }
	inline void incDirs() { scoped_lock s(slock); numDirs++; }
	inline void incDuplicates() { scoped_lock s(slock); numDuplicates++; }
	inline void incErrors() { scoped_lock s(slock); numErrors++; }
	inline void incNameErrors() { scoped_lock s(slock); numNameErrors++; }
	inline void addBytes(int32_t bc) { scoped_lock s(slock); numBytes += bc; }

	inline string progressStr()
	{
		stringstream ss;

		ss << "di: " + to_string(numDirs);
		ss << ", fi:" + to_string(numFiles);
		ss << ", im:" + to_string(numImages);
		ss << ", er:" + to_string(numErrors);
		ss << ", du:" + to_string(numDuplicates);
		ss << ", by:" + to_string(numBytes);
		ss << "";

		return ss.str();

	}
	inline string finalStr()
	{
		stringstream ss;
		ss << "Final: \nNum Threads: " + to_string(numThreads);
		ss << "\nImages: " + to_string(numImages);
		ss << "\nDuplicate Images: " + to_string(numDuplicates);
		ss << "\nFiles: " + to_string(numFiles);
		ss << "\nDirectories: " + to_string(numDirs);
		ss << "\nBytes Processed: " + to_string(numBytes);
		ss << "\nErrors: " + to_string(numErrors);
		ss << "\nName Errors: " + to_string(numNameErrors);
		ss << "\n" ;

		return ss.str();
	}

	Stats()
	{
		 numThreads=0;
		 numImages = 0;
		 numNameErrors = 0;
		 numFiles = 0;
		 numDirs = 0;
		 numDuplicates = 0;
		

		 numErrors = 0;
		 numBytes = 0;
	}

private:

	int32_t numThreads;
	int32_t numImages;
	int32_t numFiles;
	int32_t numDirs;
	int32_t numDuplicates;
	int32_t numErrors;
	int32_t numNameErrors;
	uint64_t numBytes;

};