
#include "cimage.h"



bool ImgCollection::Save(fs::path dir)
{
	string dirpart;
	string filepart;

	string tstr = stop;
	ImgUtils::Replace(tstr, "\\", "/");

	ofstream odir(dir.string() + "/dirs.txt");
	odir << tstr << endl;
	for (list<ImgCollectionDirItem*>::iterator it = dirs.begin(); it != dirs.end(); ++it)
	{
		ImgCollectionDirItem* d = *it;
		odir << d->toSave() << endl;
	}
	odir.close();

	ofstream ofile(dir.string() + "/files.txt");
	for (list<ImgCollectionFileItem*>::iterator it = files.begin(); it != files.end(); ++it)
	{
		ImgCollectionFileItem* f = *it;
		ofile << f->toSave() << endl;
	}
	ofile.close();

	ofstream oimg(dir.string() + "/images.bin", ios::out | ios::binary);
	for (map<int64_t, ImgCollectionImageItem*>::iterator it = images.begin(); it != images.end(); ++it)
	{
		int64_t icrc = it->first;
		ImgCollectionImageItem* f = it->second;
		oimg.write((char*)&icrc, sizeof(icrc));
		oimg.write((char*)f->thumb, TNMEM);
	}
	oimg.close();


	return true;
}



// Load. loads the imgcollection from disk. each of the collections is read from its own
// file. 
// ** these may have been created my one of the other imagecollection implementations so
//    we cant use serialization **
bool ImgCollection::QLoad(fs::path set)
{
	string line;
	ifstream idir(set.string() + "/dirs.txt");
	getline(idir, line);

	top = fs::path(line);
	stop = line;
	if (!fs::exists(top))
		logger::fatal("Top dir " + line + " does not exist.");


	while (getline(idir, line))
	{
		ImgCollectionDirItem* d = ImgCollectionDirItem::fromSave(line);
		if (d != nullptr)
		{
			dirs.push_back(d);
		}
	}
	idir.close();


	ifstream ifile(set.string() + "/files.txt");
	while (getline(ifile, line))
	{
		ImgCollectionFileItem* f = ImgCollectionFileItem::fromSave(line);
		if (f != nullptr)
		{
			files.push_back(f);
		}
	}
	ifile.close();


	ifstream iimg(set.string() + "/images.bin", ios::binary);
	int64_t icrc;
	int8_t thumb[TNMEM];
	int stx = 0;
	while (true)
	{
		if (iimg.read((char*)&icrc, sizeof(icrc)))
		{
			iimg.read((char*)thumb, TNMEM);
			ImgCollectionImageItem* sii = new ImgCollectionImageItem(icrc, thumb);
			// store in image map
			images[icrc] = sii;

		}
		else
			break;
	}
	iimg.close();

	return true;
}
