

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
ImgCollectionReBuild::ImgCollectionReBuild()
{
	ic = new ImgCollection();
}

void ImgCollectionReBuild::Save()
{
	ic->Save(saveTo);
}


void ImgCollectionReBuild::Load(fs::path from)
{
	ic->QLoad(from);
}



// Two ways to use the builder, either Create ( build from files ) or
// Load ( load an existing set ) 
// in either case the 'ic' will be filled
void ImgCollectionReBuild::Create(fs::path _saveTo)
{
	saveTo = _saveTo;
}

//
// walkFiles
//
// The main 'Create' method. Called initially from the 'top' directory
// we then loop through files in that dir, if we find a directory, call ourselves
// recursivly. If we find a file, call processFile to deal with it ( in 
// imgProcessingThread )
//
bool ImgCollectionReBuild::walkDirecrories(fs::path dir)
{

	string dirpart;
	string filepart;
	time_t lastmod = 0;
	bool processFiles = false;

	uint32_t dirHash = ic->pathsplit(dir, dirpart, filepart, lastmod);
	if (dirHash == 0)
	{
		st.incNameErrors();
		return false;
	}
	ImgCollectionDirItem* di = dirs[dirHash];
	if (di == nullptr)
	{
		logger::info("this is a NEW FOLDER , add to list" + dir.string());
		processFiles = true;
		ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
		st.incDirs();
	}
	else
	{
		if (lastmod > di->lmod)
		{
			di->lmod = lastmod;
			logger::info("this is an existing MODIFIED folder" + dir.string());
			processFiles = true;
		}
		else
			logger::debug("directory is unchanged");
	}

	// go thru all files/dirs
	for (fs::directory_entry de : fs::directory_iterator(dir))
	{
		if (fs::is_directory(de))
		{
			walkDirecrories(de.path());
		}
		else
		{
			if (processFiles)
			{
				logger::debug("Process file " + de.path().string() + " in " + dir.string() + " as its dir is modified or new");

				if (ImgUtils::IsImageFile(de.path()))
				{
					uint32_t fdirHash = ic->pathsplit(de, dirpart, filepart, lastmod);


					if (fdirHash == 0)
					{
						st.incNameErrors();
						continue;
					}
					uint64_t cfh = dfHash(fdirHash, filepart);
					if (files[cfh] == nullptr)
					{
						ImageInfo* ii = new ImageInfo();
						ii->de = de;
						ii->dirhash = dirHash;
						ii->filepart = filepart;
						logger::info("Process file " + de.path().string() + " in " + dir.string() + " as file is NEW");
						processItem(ii);
					}
					else
						logger::debug("Ignore file in modified dir " + de.path().string() + " in " + dir.string() + " as the file isnt new");
				}
			}
			else
				logger::debug("Ignore file " + de.path().string() + " in " + dir.string() + " as its dir is not modified");
		}
	}


	return true;
}

void ImgCollectionReBuild::processItemResult(ImageInfo* ii)
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

void ImgCollectionReBuild::processItem(ImageInfo* ii)
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

bool ImgCollectionReBuild::isSubdir(fs::path full, fs::path sub)
{
	string apath = fs::absolute(full).string();
	string tpath = fs::absolute(sub).string();

	return (apath.substr(0, tpath.length()) == tpath);

}

uint64_t ImgCollectionReBuild::dfHash(uint32_t dhash, string filename)
{
	uint32_t fhash = ImgUtils::GetHash(filename);
	uint64_t dfhash = ((uint64_t)dhash << 32) | fhash;
	return dfhash;
}

void ImgCollectionReBuild::Refresh(fs::path set)
{
	if (!isSubdir(set, ic->top))
		logger::fatal("scan path not under top");

	for (auto const& ditem : ic->dirs)
	{
		uint32_t dhash = ditem->hash;
		dirs[dhash] = ditem;
	}

	for (auto const& fitem : ic->files)
	{
		uint64_t dfhash = dfHash(fitem->dhash, fitem->name);
		files[dfhash] = fitem;
	}

	walkDirecrories(fs::absolute(set));


}

