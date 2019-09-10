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

package arenbee.jimageutils;

import java.util.Comparator;

// SearchResult. Used to return results of a search. basically an image & its closeness
// to the searched image
public class SearchResult 
{
    private ImgCollectionImageItem  image = null;
    private double  closeness = 0.0;

    @Override
    public String toString() {
        return "SearchResult{" + "image=" + image + ", closeness=" + closeness + '}';
    }

    public void setImage(ImgCollectionImageItem image) {
        this.image = image;
    }

    public void setCloseness(double closeness) {
        this.closeness = closeness;
    }

    public ImgCollectionImageItem getImage() 
    {
        return image;
    }

    public double getCloseness() 
    {
        return closeness;
    }

 
    
}

// SearchResultComparator. This is used to compare two SearchResult objects
// by the TreeSet class. Here we comapre the closeness
class SearchResultComparator implements Comparator<SearchResult>
{
    public int compare(SearchResult s1, SearchResult s2) {
        if (s1.getCloseness() < s2.getCloseness())
            return -1;
        else if (s1.getCloseness() > s2.getCloseness() )
            return 1;
        return 0;
    }
}