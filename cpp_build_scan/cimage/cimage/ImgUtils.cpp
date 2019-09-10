
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
#include "crc32.h"

vector<string> extensions{ ".bmp", ".jpg", ".jpeg", ".png", ".gif" };


int64_t ImgUtils::GetHash(string &s)
{
	int64_t xcrc = crc(s.begin(), s.end());
	return xcrc;
}


int64_t ImgUtils::GetHash(int8_t *bytes, int len)
{
	int64_t xcrc = crc(bytes,bytes+len);
	return xcrc;
}


bool ImgUtils::IsImageFile(fs::path fileStr)
{
	string extension = fileStr.extension().string();
	transform(extension.begin(), extension.end(), extension.begin(), ::tolower);

	return find(extensions.begin(), extensions.end(), extension) != extensions.end();
}

fs::path ImgUtils::Cwd()
{
	char buf[MAX_PATH];
	GetCurrentDirectoryA(256, buf);
	return fs::path(buf);
}

void ImgUtils::Replace(string& source, string const& find, string const& replace)
{
	for (string::size_type i = 0; (i = source.find(find, i)) != string::npos;)
	{
		source.replace(i, find.length(), replace);
		i += replace.length();
	}
}


bool ImgUtils::GetImageInfo(ImageInfo *ii)
{
	try
	{
		string s = ii->de.string();
		const char *filepath = s.c_str();

		ifstream ifs(filepath, ios::binary | ios::ate);
		ifstream::pos_type pos = ifs.tellg();
		ii->size = (int32_t)pos;
		ii->thumb = new int8_t[TNMEM];
		ii->bytes = new int8_t[ii->size];
		ifs.seekg(0, ios::beg);
		ifs.read((char*)ii->bytes, ii->size);
		ifs.close();

		ii->crc = ImgUtils::GetHash(ii->bytes, ii->size);

		fipMemoryIO memIO((BYTE*)ii->bytes, ii->size);
		fipImage fi;
		fi.loadFromMemory(memIO);

		bool ok = fi.flipVertical();

		ok = fi.rescale(TNSIZE, TNSIZE, FILTER_BOX);
		if (!ok)
		{
			logger::error("Image did not rescale");
			return false;
		}

		ok = fi.convertTo24Bits();
		if (!ok)
		{
			logger::error("Image did not convert to 24 bit");
			return false;
		}

		int bpp = fi.getBitsPerPixel();
		if (bpp != 24 )
		{
			logger::error("Image did not have right bits per pixel");
			return false;
		}

		int sw = fi.getScanWidth();
		if (sw != (TNSIZE * 3))
		{
			logger::error("Image did not convert to expected scanwidth");
			return false;
		}

		int pw = fi.getWidth();
		if (pw != TNSIZE)
		{
			logger::error("Image did not convert to expected width");
			return false;
		}

		int wb = 0;
		int tb = 0;

		for (int r = 0; r < pw; r++)
		{
			int8_t *sl = (int8_t*)fi.getScanLine(r);
			tb = 0;
			for (int c = 0; c < pw; c++)
			{
				ii->thumb[wb++] = sl[tb + 2];
				ii->thumb[wb++] = sl[tb + 1];
				ii->thumb[wb++] = sl[tb + 0];
				tb += 3;
			}
		}

		return true;
	}
	catch (std::exception &e)
	{
		throw new std::runtime_error("Other error:" + string(e.what()));
		return false;
	}
	catch (...)
	{
		throw new std::runtime_error("Other error:");
		return false;
	}


}


double ImgUtils::GetCloseness(int8_t *i1, int8_t *i2)// , stringstream *ss)
{

	int td = 0;



	for (int x = 0; x < TNMEM; x+=3 )
	{
		int sr = (int)(uint8_t)i1[x];
		int cr = (int)(uint8_t)i2[x];
		int sg = (int)(uint8_t)i1[x+1];
		int cg = (int)(uint8_t)i2[x+1];
		int sb = (int)(uint8_t)i1[x+2];
		int cb = (int)(uint8_t)i2[x+2];
		int d = (abs(sr - cr) + abs(sg - cg) + abs(sb - cb));
		td += d;
		/*if (ss)
		{
			*ss << ",p," << x << ",s," << sr << "," << sg << "," << sb << ",c," << cr << "," << cg << "," << cb << ",d," << d << ",td,"
				<< td;
		}*/
	}
	return (double)td;
	
}


