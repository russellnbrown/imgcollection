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

// RTimer - simple timer for use in stats..
public class RTimer {
    private long startAt = 0;
    private long endAt = 0;

    public RTimer()
    {
        reset();
    }

    public void reset()
    {
        startAt = System.currentTimeMillis();
        endAt=startAt;
    }

    public void stop()
    {
        endAt = System.currentTimeMillis();
    }

    public long elapsed()
    {
        return System.currentTimeMillis()-startAt;
    }


    @Override
    public String toString()
    {
        return String.format("%dms", endAt - startAt);
    }
}
