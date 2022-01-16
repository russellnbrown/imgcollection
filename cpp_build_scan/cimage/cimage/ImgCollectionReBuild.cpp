

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


uint32_t ImgCollectionReBuild::processWalkedDir(fs::path dir)
{
	string dirpart;
	string filepart;
	time_t lastmod = 0;
	bool processFiles = false;

	logger::info("DIR " + dir.string());

	uint32_t dirHash = ic->pathsplit(dir, dirpart, filepart, lastmod);
	if (dirHash == 0)
	{
		logger::info(" - name error");
		st.incNameErrors();
		return dirHash;
	}

	logger::info(" - " + dirpart);

	ImgCollectionDirItem* di = dirs[dirHash];
	bool isNew = false;
	bool isModded = false;
	if (di != nullptr)
	{
		logger::info(" - known");
		numKnownDirs++;
		if (lastmod > di->lmod)
		{
			isModded = true;
			di->lmod = lastmod;
			logger::info(" - modified");
			numModDirs++;
		}
		ic->dirs.push_back(di);
	}
	else
	{
		numNewDirs++;
		logger::info(" - new");
		isNew = true;
		ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
	}

	return dirHash;
}

bool ImgCollectionReBuild::processWalkedFile(fs::path fil)
{
	string dirpart;
	string filepart;
	time_t lastmod = 0;

	logger::info("FIL " + fil.string());
	uint32_t dirHash = ic->pathsplit(fil, dirpart, filepart, lastmod);

	if (dirHash == 0)
	{
		logger::info(" - name error");
		st.incNameErrors();
		return false;
	}

	string dfhash = dfHash(dirHash, filepart);
	
	ImgCollectionFileItem *fi = files[dfhash];
	if (fi != nullptr)
		numKnownFiles++;
	else
		numNewFiles++;


	if (fi != nullptr)
	{
		logger::info(" - " + filepart + " known");
		ic->files.push_back(fi);
		return true;
	}
	logger::info(" - " + filepart + " new" );

	if (ImgUtils::IsImageFile(fil) )
	{
		numImageFiles++;
		ImageInfo* ii = new ImageInfo();
		ii->de = fil;
		ii->dirhash = dirHash;
		ii->filepart = filepart;
		processItem(ii);
		return true;
	}
	else if (ImgUtils::IsVideoFile(fil))
	{
		numVideoFiles++;
	}
	else 
	{
		numOtherFiles++;
	}
	ImageInfo* ii = new ImageInfo();
	ii->de = fil;
	ii->dirhash = dirHash;
	ii->filepart = filepart;
	ic->files.push_back(new ImgCollectionFileItem(ii->dirhash, ii->crc, ii->filepart));

	//ic->files.push_back(nullptr);
	return true;
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
	numDirs++;
	uint32_t dhash = processWalkedDir(dir);

	for (fs::directory_entry de : fs::directory_iterator(dir))
	{
		if (fs::is_directory(de))
		{
			walkDirecrories(de.path());
		}
		else
		{
			numFiles++;
			processWalkedFile(de);
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

string ImgCollectionReBuild::dfHash(uint32_t dhash, string filename)
{
	uint32_t fhash = ImgUtils::GetHash(filename);
	
	char s[1024];
	snprintf(s, 1024, "%u_%s", dhash, filename.c_str());// fhash);
	return s;
}

void ImgCollectionReBuild::dump()
{
	char txt[MAX_PATH];
	snprintf(txt, MAX_PATH, "Dirs: %d (%d known) (%d new) (%d mod) Files: %d (%d known) (%d new) (%d newimg) (%d newvid) (%d newother)",
		numDirs, numKnownDirs, numNewDirs, numModDirs,
		numFiles, numKnownFiles, numNewFiles, numImageFiles, numVideoFiles, numOtherFiles);
	logger::info(txt);
}

void ImgCollectionReBuild::Rebuild()
{


	for (auto const& ditem : ic->dirs)
	{
		uint32_t dhash = ditem->hash;
		dirs[dhash] = ditem;
	}

	for (auto const& fitem : ic->files)
	{
		string dfhash = dfHash(fitem->dhash, fitem->name);
		files[dfhash] = fitem;
	}

	logger::debug("Existing set has: dirs:" + to_string(dirs.size()) + ",  files:" + to_string(files.size()) + ", images:" + to_string(ic->images.size()));

	ic->dirs.clear();
	ic->files.clear();

	walkDirecrories(fs::absolute(ic->stop));

	dump();

}

