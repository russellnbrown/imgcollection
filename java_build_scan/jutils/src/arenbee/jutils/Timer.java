/*
 * Copyright (C) 2019 russe
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

import arenbee.jutils.RTimer;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author russe
 */
public class Timer 
{
    private static HashMap<String,Long> stages = new LinkedHashMap<String,Long>();
    private static RTimer stagetimer = new RTimer();
  
    public static void stagestart()
    {
        stagetimer.reset();
    }

    public static void stagestop(String stageName)
    {
        stagetimer.stop();
        Long took = stagetimer.elapsed();
        stages.put(stageName, took);
    }    
    
    public static String stagereport(String title)
    {
        StringBuilder b = new StringBuilder();
        b.append("\n");
        b.append(title);
        b.append("\n");
        stages.forEach((k,v) -> b.append( k+" : "+v+" ms.\n"));
        b.append("\n");
        return b.toString();
    }

}
