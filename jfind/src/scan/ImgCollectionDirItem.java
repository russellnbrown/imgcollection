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


// ImgCollectionDirItem. Holds information about a directory in the collection
public class ImgCollectionDirItem 
{
    private String dir;     // path ( relative to the top )
     private long  hash;     // crc32 hash of path
    private long  lmod;     // crc32 hash of path

    public long getLmod() {
        return lmod;
    }

    public void setLmod(long lmod) {
        this.lmod = lmod;
    }

    @Override
    public String toString() 
    {
        return "SetItemDir{" + "dir=" + dir + ", hash=" + Long.toHexString(hash) +'}';
    }

    public String getDir() 
    {
        return dir;
    }

    public void setDir(String dir) 
    {
        this.dir = dir;
    }

    public long getHash() 
    {
        return hash;
    }

    public void setHash(long hash) 
    {
        this.hash = hash;
    }


}
