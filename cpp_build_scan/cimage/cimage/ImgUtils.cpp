
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



double ImgUtils::GetCloseness(int8_t* i1, int8_t* i2, SearchType scanType)
{
	float rd = 0.0;
	float gd = 0.0;
	float bd = 0.0;
	double td = 0.0;

	if (scanType == Assembler)
		return GetAsmCloseness(i1, i2);



	for (int tix = 0; tix < TNMEM; tix+=3)
	{
		double srx = i1[tix];
		double crx = i2[tix];
		double sgx = i1[tix+1];
		double cgx = i2[tix+1];
		double sbx = i1[tix+2];
		double cbx = i2[tix+2];

		switch (scanType)
		{
			case Mono:
			{
				double lums = ((double)srx * 0.21) + ((double)sgx * 0.72) + ((double)sbx * 0.07);
				double lumc = ((double)crx * 0.21) + ((double)cgx * 0.72) + ((double)cbx * 0.07);
				td += abs(lums - lumc) / 255.0;
			}
			break;
			case Simple:
			{
				int tx = abs(sbx - cbx) + abs(sgx - cgx) + abs(srx - crx);
				td += (double)tx;
			}
			break;
			case Luma:
			{
				double dxr = abs(srx - crx) * 0.21;
				double dxg = abs(sgx - cgx) * 0.72;
				double dxb = abs(sbx - cbx) * 0.07;
				double tx = (dxr + dxg + dxb);
				td += tx;
			}
		}
	}

	return td;

}


double ImgUtils::GetAsmCloseness(int8_t* i1, int8_t* i2)
{
	uint32_t diff = 0;
#ifdef WIN64
	logger::error("assembler option not supported on 64 bit arch");
	return 0;
#else
	char* s = (char*)i1;
	char* p = (char*)i2;

	_asm
	{
		// move start pointers of thumbnails to edi,esi
		mov edi, i1
		mov esi, i2
		// edx will be a counter for number of pixels to compare
		mov edx, TNSIZE* TNSIZE * 3
		// eax will hold total difference for all pixels
		mov eax, 0

		label:

			// copy pixel data from thumbs to ecx,ebx
			mov ecx, [edi]
			mov ebx, [esi]

			// mask out anybits > 255
			and ecx, 0xff
			and ebx, 0xff

			// compare the two numbers, if needed swap them so the
			// bigger number is in eax
			cmp ecx, ebx
			jge noex
			xchg  ecx, ebx
			noex :

			// subtract to get the difference
			sub ecx, ebx
			// add diff to total
			add eax, ecx
			// inc the pixel pointers
			inc edi
			inc esi
			// dec pixel count and process next if needed
			dec edx
			cmp edx, 0
			jnz label

			// finished for thumbs, copy final difference to 'diff'
			mov diff, eax
	}
	return (double)diff;
#endif

}
