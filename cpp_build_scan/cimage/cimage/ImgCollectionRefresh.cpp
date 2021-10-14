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


#include "cimage.h"


 // ImgCollectionBuilder. This is a helper class to build an Image Collection.
 // It can use a simple or multithreaded approach.


// constructor. set the singleton
ImgCollectionRefresh::ImgCollectionRefresh()
{
	ic = new ImgCollection();
}

void ImgCollectionRefresh::Save()
{
	ic->Save(saveTo);
}


void ImgCollectionRefresh::Load(fs::path from)
{
	ic->QLoad(from);
}



// Two ways to use the builder, either Create ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
void ImgCollectionRefresh::Create(fs::path _saveTo)
{
	saveTo = _saveTo;
}

int64_t ImgCollectionRefresh::pathsplit(const fs::path d, string& dirpart, string& filepart, time_t& lastMod)
{
	try
	{
		lastMod = 0;
		struct stat result;
		if (stat(d.string().c_str(), &result) == 0)
			lastMod = result.st_mtime;


		if (fs::is_regular_file(d))
		{
			filepart = d.filename().string();
			dirpart = d.parent_path().string();
		}
		else if (fs::is_directory(d))
		{
			filepart = "";
			dirpart = d.string();
		}
		else
			return 0;
	}
	catch (std::exception e)
	{
		logger::warn("Ignore ");
		return 0;
	}

	ImgUtils::Replace(dirpart, "\\", "/");
	dirpart = dirpart.substr(ic->top.string().length());

	if (dirpart.empty() || dirpart[0] != '/')
		dirpart = "/" + dirpart;

	return ImgUtils::GetHash(dirpart);
}

//
// walkFiles
//
// The main 'Create' method. Called initially from the 'top' directory
// we then loop through files in that dir, if we find a directory, call ourselves
// recursivly. If we find a file, call processFile to deal with it ( in 
// imgProcessingThread )
//
bool ImgCollectionRefresh::walkFiles(fs::path dir)
{

	string dirpart;
	string filepart;
	time_t lastmod = 0;

	// add 'top' directory to the collection 
	int64_t dirHash = pathsplit(dir, dirpart, filepart, lastmod);
	ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
	st.incDirs();

	// loop for all its files/subdirs
	int fcntr = 0;
	for (fs::directory_entry de : fs::recursive_directory_iterator(dir))
	{
		string dirpart;
		string filepart;

		// get crc32 of the directory path
		int64_t dirHash = pathsplit(de, dirpart, filepart, lastmod);
		if (dirHash == 0)
		{
			st.incNameErrors();
			continue;
		}

		// givee occasional feedback
		if (++fcntr % 5000 == 0)
		{
			logger::info("At: " + st.progressStr());
		}

		// if its a directory, create a DirItem and add to collection
		if (filepart.empty())
		{
			st.incDirs();
			ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
			logger::debug("Dir is " + dirpart);
		}
		else
		{
			// if its an image file, create a ImageInfo and process it
			st.incFiles();
			if (ImgUtils::IsImageFile(de.path()))
			{
				ImageInfo* ii = new ImageInfo();
				ii->de = de;
				ii->dirhash = dirHash;
				ii->filepart = filepart;
				processItem(ii);
			}
		}
	}

	return true;
}

void ImgCollectionRefresh::processItemResult(ImageInfo* ii)
{
	//logger::info("Results " + ii->filepart);
	string err;

	st.addBytes(ii->size);
	{
		try
		{
			ic->files.push_back(new ImgCollectionFileItem(ii->dirhash, ii->crc, ii->filepart));
			if (ic->images.find(ii->crc) == ic->images.end())
			{
				ic->images[ii->crc] = new ImgCollectionImageItem(ii->crc, ii->thumb);
				st.incImages();
			}
			else
				st.incDuplicates();
		}
		catch (exception& e)
		{
			st.incErrors();
			err = "Could not gen image " + ii->filepart + string(e.what());
		}
	}

	delete ii;


	if (err.length() > 0)
		logger::error(err);

}

void ImgCollectionRefresh::processItem(ImageInfo* ii)
{
	// process an image file

	try
	{

		if (ImgUtils::GetImageInfo(ii))
			processItemResult(ii);
		else
			st.incErrors();

	}
	catch (runtime_error& e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}
	catch (exception& e)
	{
		logger::error("Could not gen image");
		printf("%s\n", e.what());
		printf("\n");
		st.incErrors();
	}

}


void ImgCollectionRefresh::Refresh(fs::path set)
{

}

