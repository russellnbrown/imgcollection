/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arenbee.api;

import java.util.*;

public class GenericSearchResult
{
    public String message;
    public String top;
    public ArrayList<GenericSearchResultItem> items = new ArrayList<>();
    public GenericSearchResult()
    {
        
    }
    public GenericSearchResult(String top)
    {
        this.top = top;
    }
    
}