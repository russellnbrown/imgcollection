/*
 * Copyright (C) 2018 Russell Brown
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

import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;

// Gen. Some useful helpers
public class Gen
{

    private static String OS = System.getProperty("os.name").toLowerCase();
    public static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    public static DateFormat dfs = new SimpleDateFormat("HH:mm:ss");

    // Return formatted date
    public static String DateStr(Date d)
    {
        return df.format(d);
    }

    // Return formatted date
    public static String ShortDateStr(Date d)
    {
        return dfs.format(d);
    }

    // Sleep for a number of milliseconds. 
    public static void Sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        } catch (InterruptedException ex)
        {
        }
    }

    // Check extension to see if this is an image file
    public static boolean IsImageFile(Path fileName)
    {
        String fileStr = fileName.toString();
        String extension = "";

        int i = fileStr.lastIndexOf('.');
        if (i > 0)
        {
            extension = fileStr.substring(i + 1).toLowerCase();
            if (extension.equals("jpg"))
            {
                return true;
            }
            if (extension.equals("png"))
            {
                return true;
            }
            if (extension.equals("bmp"))
            {
                return true;
            }
            if (extension.equals("jpeg"))
            {
                return true;
            }
            if (extension.equals("gif"))
            {
                return true;
            }
        }

        return false;
    }

    // Calculate CRC32 hash for a byte attay
    public static long Hash(byte[] bytes)
    {

        CRC32 crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }

    // StandardizePath. changes \\ to / 
    // makes any drive specification to capital letter
    public static String StandardizePath(String p)
    {
        String path = p.replace("\\", "/"); // a\c -> a/c
        path = path.replace("//", "/");// a\c -> a/c
        if (path.length() > 2 && path.charAt(1) == ':')
        {
            path = path.substring(0, 1).toUpperCase() + path.substring(1);
        }
        return path;
    }

    public static String StandardizePath(Path p)
    {
        return StandardizePath(p.toString());
    }

}
