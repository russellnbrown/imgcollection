

package arenbee.jfxview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class TextSearchItems
{

    private  final SimpleStringProperty name; 
    private  final SimpleStringProperty type;

    public ScanSet.SSDir getDir() {
        return dir;
    }

    public List<ScanSet.SSFile> getFilelist() {
        return filelist;
    }

    private  ScanSet.SSDir dir = null;
    private List<ScanSet.SSFile> filelist= null;
    
    public TextSearchItems(ScanSet.SSDir d)
    {
        name = new SimpleStringProperty(d.GetDiskPath());
        type = new SimpleStringProperty("D");
        dir=d;
    }
    
    public TextSearchItems(List<ScanSet.SSFile> f)
    {
        name = new SimpleStringProperty("<files> " + Integer.toString(f.size()));
        type = new SimpleStringProperty("F");
        filelist = f;
    }
   
    public StringProperty nameProperty() 
    {
        return name;
    }

    public StringProperty typeProperty() 
    {
        return type;
    }


    @Override
    public String toString()
    {
        if ( dir != null )
            return "TSI[DIR " + dir.toString() + "]";
        else
            return "TSI[FILES(" + filelist.size() + ")]";
    }
}
