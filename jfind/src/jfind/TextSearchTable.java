package jfind;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

class TextSearchTable extends AbstractTableModel {

    public class RowItem {
        public String path;
        public RowItem(String s) {
            path = s;
        }
    }

    private String[] columnNames = {"Path"};
    private List<RowItem> data = new ArrayList<RowItem>();

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return data.get(row).path;
            //   case 1: return data.get(row).open;
        }
        return "";
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void Clear() {
        data.clear();
    }

    public void Add(String s) {
        data.add(new RowItem(s));

    }

    public void Finished() {
        fireTableRowsInserted(0, data.size() - 1);
    }
}
