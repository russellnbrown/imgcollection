

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

	logger::info("DIR " + dir.string() );

	uint32_t dirHash = ic->pathsplit(dir, dirpart, filepart, lastmod);
	if (dirHash == 0)
	{
		logger::info(" - name error");
		st.incNameErrors();
		return false;
	}

	numDirs++;
	logger::info("In dir " + dirpart);

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


	for (fs::directory_entry de : fs::directory_iterator(dir))
	{
		if (fs::is_directory(de))
		{
			walkDirecrories(de.path());
		}
		else
		{
			numFiles++;
			if (isNew || isModded)
			{
				logger::info("Process files in " + de.path().string() + " as its dir is modified or new");

				if (ImgUtils::IsImageFile(de.path()))
				{
					numImageFiles++;
				}
				else if (ImgUtils::IsVideoFile(de.path()))
				{
					numVideoFiles++;
				}
			}
			else
				numIgnoredFiles++;
		}
	}
	return true;

	/*


	ic->dirs.push_back(new ImgCollectionDirItem(dirHash, dirpart, lastmod));
	st.incDirs();

	ImgCollectionDirItem* di = dirs[dirHash];
	processFiles = true;
	if (di == nullptr)
	{
		logger::info("this is a NEW FOLDER , add to list" + dir.string());
		
	}
	else
	{
		if (lastmod > di->lmod)
		{
			di->lmod = lastmod;
			logger::info("this is an existing MODIFIED folder" + dir.string());
			
		}
		else
			logger::debug("directory is unchanged" + dir.string());
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

					string dfhash = dfHash(fdirHash, filepart);
					ImgCollectionFileItem* fi = files[dfhash];
					logger::info("File " + de.path().string() + " in " + dir.string() + " hash is " + dfhash + " entry is " + (fi == NULL ? "NULL" : "Found"));

					if (fi == nullptr)
					{
						ImageInfo* ii = new ImageInfo();
						ii->de = de;
						ii->dirhash = dirHash;
						ii->filepart = filepart;
						logger::debug("Process file " + de.path().string() + " in " + dir.string() + " as file is NEW");
						processItem(ii);
					}
					else
						ic->files.push_back(fi);
				}
			}
			else
				logger::debug("Ignore file " + de.path().string() + " in " + dir.string() + " as its dir is not modified");
		}
	}


	return true;
	*/
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
	logger::info("Final result dirs:" + to_string(numDirs) + "(" + to_string(numKnownDirs) + ")known (" + to_string(numModDirs) + ")mod (" + to_string(numNewDirs) + ")new, Files:" + to_string(numFiles) + " (" + to_string(numImageFiles) + ")images  (" + to_string(numVideoFiles) + ")video (" + to_string(numIgnoredFiles) + ")ignored.");

}

