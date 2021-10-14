
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