// jtable related utilities, renderers
package com.rosamez.jdvdkat.jtables;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.Comparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * right aligned number cell and its comparator
 * @author Szabolcs Kurdi
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Long) {
            String strNum = NumberFormat.getInstance().format((Long) value);
            this.setText(strNum);
        }
        setHorizontalAlignment(SwingConstants.RIGHT);
        return this;
    }

    public Comparator comparator() {
        Comparator c = new Comparator<Long>() {

            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        };
        return c;
    }
}
