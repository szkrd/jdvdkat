/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.jtables.CenterStringCellRenderer;
import com.rosamez.jdvdkat.jtables.DateCellRenderer;
import com.rosamez.jdvdkat.jtables.NumberCellRenderer;
import com.rosamez.jdvdkat.nodes.NodeGeneric;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * controller for the searchview's result table
 * @author Szabolcs Kurdi
 */
class SearchTableNodesController {
    private JDvdKatApp app = JDvdKatApp.getApplication();
    private SearchTableNodesController self = this;

    public int[] preferredColWidths = new int[6]; // TODO: do not hardcode column count!
    private int numberColNum = 3;
    private int dateColNum = 5;
    private int centeredColNum = 4;
    private boolean winJump = false;
    private JTable control;
    private SearchTableNodesModel controlModel = new SearchTableNodesModel();
    private Color defaultColor;
    public long lastRunTime = 0;

    public SearchTableNodesController(JTable control) {
        this.control = control;

        ListSelectionListener listener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                self.onRowSelectionChange();
            }
        };
        control.getSelectionModel().addListSelectionListener(listener);
        control.setShowGrid(false);

        defaultColor = control.getBackground();
        controlModel.clear();
        control.setModel(controlModel);
        control.setEnabled(false);
    }

    /**
     * setup the column rendering and sorting (we have a number and a date
     * column) - probably this is far from perfect
     */
    private void setupColumns() {
        TableRowSorter<SearchTableNodesModel> sorter =
                new TableRowSorter<SearchTableNodesModel>(controlModel);

        // date column
        TableColumn column = control.getColumnModel().getColumn(dateColNum);
        DateCellRenderer dcRend = new DateCellRenderer();
        column.setCellRenderer(dcRend);
        sorter.setComparator(dateColNum, dcRend.comparator());

        // number column
        column = control.getColumnModel().getColumn(numberColNum);
        NumberCellRenderer ncRend = new NumberCellRenderer();
        column.setCellRenderer(ncRend);
        sorter.setComparator(numberColNum, ncRend.comparator());

        column = control.getColumnModel().getColumn(centeredColNum);
        column.setCellRenderer(new CenterStringCellRenderer());

        setColWidths();
        control.setRowSorter(sorter);
    }

    public void setColWidths(int[] cols) {
        preferredColWidths = cols;
        setColWidths();
    }

    private void setColWidths() {
        TableColumn column;
        if (preferredColWidths != null) {
            for (int i = 0; i < preferredColWidths.length; i++) {
                int j = preferredColWidths[i];
                if (j > 0) {
                    column = control.getColumnModel().getColumn(i);
                    if (column != null) {
                        column.setPreferredWidth(j);
                    }
                }
            }
        }
    }

    public int[] getColWidths() {
        TableColumnModel tcm = control.getColumnModel();
        TableColumn column;
        for (int i = 0; i < preferredColWidths.length; i++) {
            column = tcm.getColumn(i);
            if (column != null) {
                preferredColWidths[i] = column.getWidth();
            }
        }
        return preferredColWidths;
    }

    /**
     * populate the table - in fact it is the search itself
     * @param fileName  use an empty string to search in all the docs
     * @param path  xpath expression
     * @return
     */
    public int populate(String fileName, String path) {
        long startTime = System.nanoTime();
        ArrayList<NodeGeneric> nodes = app.dvdKatModel.getNodesForFile(fileName, path);
        lastRunTime = System.nanoTime() - startTime;
        if (nodes == null) {
            return 0;
        }
        control.setEnabled(true);
        controlModel.setData(nodes, false);
        control.setModel(controlModel);
        toRow(0);
        control.setShowGrid(false);
        control.revalidate();
        setupColumns();
        return nodes.size();
    }

    /**
     * go to given row
     * @param idx
     */
    protected void toRow(int idx) {
        control.getSelectionModel().setSelectionInterval(idx, idx);
        Rectangle cellRect = control.getCellRect(idx, 0, true);
        control.scrollRectToVisible(cellRect);
    }

    /**
     * callback for on selection change: call the main window
     * and have it jumped to the found file if requested
     */
    protected void onRowSelectionChange() {
        doWinJump();
    }

    public void doWinJump() {
        if (winJump) {
            NodeGeneric selNode = getCurrentNode();
            String shortDoc = selNode.getShortDoc();
            app.mainWindow.goToDocPath(shortDoc, selNode.unescapedAbsolutePath, selNode.name);
        }
    }

    /**
     * get node that corresponds to the selected row
     * @return
     */
    public NodeGeneric getCurrentNode() {
        int rowIndex = control.getSelectedRow();
        rowIndex = control.convertRowIndexToModel(rowIndex); // for the sorter
        return controlModel.getNodeAt(rowIndex);
    }

    /**
     * allow jump to functionality (main window)?
     * @param selected
     */
    public void allowMainWinJump(boolean selected) {
        winJump = selected;
    }

    public void gotoFirstNode() {
        toRow(0);
    }

    public void gotoLastNode() {
        int lastIdx = controlModel.getRowCount() - 1;
        toRow(lastIdx);
    }
}
