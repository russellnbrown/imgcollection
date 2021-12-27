package jfind;

import javax.swing.table.AbstractTableModel;

class ImgSearchTable extends AbstractTableModel {

    private String[] columnNames = {"Bob", "Dir", "File", "Match", "Open"};

    private Object[][] data = {
        {"Mary", "Campione", "Snowboarding", new Integer(5),
            new Boolean(false)},
        {"Alison", "Huml", "Rowing", new Integer(3), new Boolean(true)},
        {"Kathy", "Walrath", "Knitting", new Integer(2),
            new Boolean(false)},
        {"Sharon", "Zakhour", "Speed reading", new Integer(20),
            new Boolean(true)},
        {"Philip", "Milne", "Pool", new Integer(10),
            new Boolean(false)}};

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
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

    private boolean DEBUG = false;

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                    + " to " + value + " (an instance of "
                    + value.getClass() + ")");
        }

        data[row][col] = value;
        fireTableCellUpdated(row, col);

    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print("  " + data[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}
