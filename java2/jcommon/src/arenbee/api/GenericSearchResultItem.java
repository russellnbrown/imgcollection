/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arenbee.api;

/**
 *
 * @author russ
 */
public class GenericSearchResultItem
{
    public String path;
    public String file;
    public int closeness;
    public GenericSearchResultItem(String s)
    {
        path = s;
        file="";
        closeness = 0;
    }
    
    public GenericSearchResultItem(String s, String f)
    {
        path = s;
        file = f;
        closeness = 0;
    }
}
