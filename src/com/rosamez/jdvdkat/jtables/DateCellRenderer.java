// jtable related utilities, renderers
package com.rosamez.jdvdkat.jtables;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Date cell and its comparator
 * @author Szabolcs Kurdi
 */
public class DateCellRenderer extends DefaultTableCellRenderer {

    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd. kk:mm");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Date) {
            String strDate = dateFormatter.format((Date) value);
            this.setText(strDate);
        }

        return this;
    }

    public Comparator comparator() {
        Comparator c = new Comparator<Date>() {

            @Override
            public int compare(Date o1, Date o2) {
		Long ol1 = o1.getTime();
		Long ol2 = o2.getTime();
		return ol1.compareTo(ol2);
            }
        };
        return c;
    }
}
