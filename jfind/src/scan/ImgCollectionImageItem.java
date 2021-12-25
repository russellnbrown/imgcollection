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
package scan;

import jutils.Logger;
import static java.lang.Math.abs;

// ImgCollectionImageItem. Holds information about an image in the collection
public class ImgCollectionImageItem
{

    private long ihash;     // crc32 hash of the image in the file
    private byte[] thumb;   // rgb 16*16 thumb of the image
    private int length;     // length of file

    public int getSize()
    {
        return length;
    }

    public long getIHash()
    {
        return ihash;
    }

    public byte[] getThumb()
    {
        return thumb;
    }

    public void setThumb(byte[] bytes)
    {
        this.thumb = bytes;
    }

    public void setCrc(long crc)
    {
        this.ihash = crc;
    }

    public void setSize(int l)
    {
        this.length = l;
    }

    // getCVal.  Compares this image to another and returns a 'closeness' 
    public double getCVal(ImgCollectionImageItem ji)//, StringBuilder ss)
    {
        // if hashes are the same, the image is identical
        if (ji.ihash == ihash)
        {
            // exact match
            return 0;
        } else
        {
            // otherwise calc a difference for each pixel and a total difference
            // ** this is a 'simple' value difference. there are better ways which
            // are explored in the cimagex c++ code **
            double td = 0.0;
            for (int tix = 0; tix < 16 * 16 * 3; tix += 3) {
                int sr = thumb[tix] & 0xFF;
                int cr = ji.thumb[tix] & 0xFF;
                int sg = thumb[tix + 1] & 0xFF;
                int cg = ji.thumb[tix + 1] & 0xFF;
                int sb = thumb[tix + 2] & 0xFF;
                int cb = ji.thumb[tix + 2] & 0xFF;
                int d = (abs(sr - cr) + abs(sg - cg) + abs(sb - cb));
                td += d;

            }
            
   
            return td;
        }
    }

    @Override
    public String toString()
    {
        return "SetItemImage{" + "crc=" + Long.toHexString(ihash) + '}';
    }

}
