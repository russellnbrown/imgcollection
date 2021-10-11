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

package com.arenbee.imglib;

// ImgCollectionFileItem. Holds information about a file in the collection
public class ImgCollectionFileItem 
{
    private String file;    // name
    private long dhash;     // hash of dirent the file belongs to
    private long ihash;     // crc32 hash of the image bytes in the file

    public long getIHash() 
    {
        return ihash;
    }

    public void setIHash(long crc) 
    {
        this.ihash = crc;
    }

    public String getFile() 
    {
        return file;
    }

    public void setFile(String file) 
    {
        this.file = file;
    }

    public long getDhash() 
    {
        return dhash;
    }

    public void setDhash(long dhash) 
    {
        this.dhash = dhash;
    }
    
    public ImgCollectionFileItem()
    {        
    }

    @Override
    public String toString() 
    {
        return "SetItemFile{" + "file=" + file + ", crc=" + Long.toHexString(ihash) + ", dhash=" + Long.toHexString(dhash) + '}';
    }
    
}
