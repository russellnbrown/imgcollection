
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

 //
 // ImgCollectionBuilder
 //
 // This is a helper class to build an Image Collection and to
 // provide various methods to access it
 //


class finfo
{
public:

	finfo(fs::path f)
	{
		full = fs::absolute(f);

		lmod = 0;
		struct stat result;
		if (stat(full.string().c_str(), &result) == 0)
			lmod = result.st_mtime;


		if (fs::is_regular_file(full))
		{
			filepart = full.filename().string();
			dirpart = full.parent_path().string();
		}
		else if (fs::is_directory(full))
		{
			filepart = "";
			dirpart = full.string();
			isDir = true;
		}
	}
	fs::path	full;
	fs::path	dirpart;
	string		filepart;
	bool		isDir = false;
	uint32_t	dhash;
	time_t		lmod = 0;

};

class ImgCollectionTester
{


private:

	ImgCollection* ic = nullptr;			// The ImgCollection

	Stats st;								// stats on processing
	ImageInfo* searchItem;					// in searches, the image being searched for
	fs::path saveTo;

public:
	ImgCollectionTester();

public:

	void Test(int argc, char *argv[]);

private:
	bool walkDirecrories(fs::path dir);			// iterates over files in a directory tree 
	uint32_t pathsplit(const fs::path d, string& dir, string& file, time_t& lastMod);

	map<uint32_t, ImgCollectionDirItem*> dirs;
	map<uint64_t, ImgCollectionFileItem*> files;


};

