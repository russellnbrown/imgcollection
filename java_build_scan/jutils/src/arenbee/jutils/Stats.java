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
package arenbee.jutils;


// Stats. Used to collect some counts & timings
public class Stats
{
    public void incImages()
    {
        numImages++;
    }

    public void incThreads()
    {
        numThreads++;
    }

    public void incFiles()
    {
        numFiles++;
    }

    public int getFiles()
    {
        return numFiles;
    }

    public void incDirs()
    {
        numDirs++;
    }

    public void incDuplicates()
    {
        numDuplicates++;
    }

    public void incErrors()
    {
        numErrors++;
    }

    public void addBytes(int bc)
    {
        numBytes += bc;
    }

    public void start()
    {
        timer.reset();
    }

    public void stop()
    {
        timer.stop();
        took = timer.elapsed();
    }
    
 
    private RTimer timer = new RTimer();
 
    @Override
    public String toString()
    {
        StringBuilder ss = new StringBuilder();
        ss.append("Stats[threads: " + numThreads);
        ss.append(", images: " + numImages);
        ss.append(", duplicates:: " + numDuplicates);
        ss.append(", files: " + numFiles);
        ss.append(", dirs: " + numDirs);
        ss.append(", bytes: " + numBytes);
        ss.append(", errors: " + numErrors);
        ss.append(", time: " + (double)took/1000.0 + " seconds." );
        return ss.toString();
    }

    public Stats()
    {
        numThreads = 0;
        numImages = 0;
        numFiles = 0;
        numDirs = 0;
        numDuplicates = 0;
        numErrors = 0;
        numBytes = 0;
    }

    private int numThreads;
    private int numImages;
    private int numFiles;
    private int numDirs;
    private int numDuplicates;
    private int numErrors;
    private long numBytes;
    private long took;

};
