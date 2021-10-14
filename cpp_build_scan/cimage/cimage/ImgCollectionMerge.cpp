#include "cimage.h"


//
// ImgCollectionSearch
// ImgCollectionSearch is a class to load and search an image database.
//
ImgCollectionMerge::ImgCollectionMerge(fs::path saveAs)
{
	// ic holds the structures constituting the database
	ic = new ImgCollection();
	saveTo = saveAs;
}

void ImgCollectionMerge::Save()
{
	ic->Save(saveTo);
}

void ImgCollectionMerge::Append(fs::path setToLoad)
{



	string line;
	ifstream idir(setToLoad.string() + "/dirs.txt");
	getline(idir, line);

	if (ic->stop.length() == 0) // first file to be merged. Determins top for all
	{
		ic->top = fs::path(line);
		ic->stop = line;
		if (!fs::exists(ic->top))
			logger::fatal("Top dir " + line + " does not exist.");
	}
	else
	{
		if (ic->stop != line)
			logger::fatal("Top dosn't match");
	}


	while (getline(idir, line))
	{
		ImgCollectionDirItem* d = ImgCollectionDirItem::fromSave(line);
		if (d != nullptr)
		{
			ic->dirs.push_back(d);
		}
	}
	idir.close();

	ifstream ifile(setToLoad.string() + "/files.txt");
	while (getline(ifile, line))
	{
		ImgCollectionFileItem* f = ImgCollectionFileItem::fromSave(line);
		if (f != nullptr)
		{
			ic->files.push_back(f);
		}
	}
	ifile.close();

	ifstream iimg(setToLoad.string() + "/images.bin", ios::binary);
	int64_t icrc;
	int8_t thumb[TNMEM];
	int stx = 0;
	while (true)
	{
		if (iimg.read((char*)&icrc, sizeof(icrc)))
		{
			iimg.read((char*)thumb, TNMEM);
			ImgCollectionImageItem* sii = new ImgCollectionImageItem(icrc, thumb);
			ic->images[icrc] = sii;
		}
		else
			break;
	}
	iimg.close();

}
