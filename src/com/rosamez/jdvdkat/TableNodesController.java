package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.jtables.CenterStringCellRenderer;
import com.rosamez.jdvdkat.jtables.DateCellRenderer;
import com.rosamez.jdvdkat.jtables.NumberCellRenderer;
import com.rosamez.jdvdkat.nodes.NodeGeneric;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author Szabolcs Kurdi
 */
public class TableNodesController {
    private JDvdKatApp app = JDvdKatApp.getApplication();
    private JTable control;
    private TableNodesModel controlModel = new TableNodesModel();
    private String returnTo = null;
    private JTextPane attribLister;
    private Color defaultColor;

    public TableNodesController(JTable control, JTextPane attribLister) {
        this.control = control;
        this.attribLister = attribLister;

        TableNodesSelectionListener listener = new TableNodesSelectionListener(this);
        control.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        control.getSelectionModel().addListSelectionListener(listener);
        control.setShowGrid(false);

        TableColumn column = control.getColumnModel().getColumn(3);
        column.setCellRenderer(new DateCellRenderer());

        defaultColor = control.getBackground();
    }

    private void setupColumns() {
        TableColumn column = control.getColumnModel().getColumn(1);
        NumberCellRenderer ncRend = new NumberCellRenderer();
        column.setCellRenderer(ncRend);

        column = control.getColumnModel().getColumn(2);
        column.setCellRenderer(new CenterStringCellRenderer());
    }

    protected void onRowSelectionChange() {
        updateAttribLister();
    }

    protected void onPopulate() {
        updateAttribLister();
    }

    /**
     * Update the textarea in the rightmost column with this nodes' attributes
     */
    private void updateAttribLister() {
        NodeGeneric actNode = getCurrentNode();
        AttributePrinter attrPrinter;
        if (actNode != null) {
            attrPrinter = new AttributePrinter(actNode);
            attribLister.setStyledDocument(attrPrinter.getStyledDocument());
            return;
        }
        attribLister.setText("");
    }

    public void populateForRoot() {
        populate("/directory/*");
    }

    public void populate(String path) {
        enableAll();
        int gotoIdx = 0;
        ArrayList<NodeGeneric> nodes = app.dvdKatModel.getNodesForFile(path);
        controlModel.setData(nodes, true);
        if (returnTo != null) {
            gotoIdx = controlModel.getIdxForName(returnTo);
            returnTo = null;
        }
        control.setModel(controlModel);
        toRow(gotoIdx);
        setupColumns();
        control.setShowGrid(false);
        control.revalidate();
        this.onPopulate();
    }

    public NodeGeneric getCurrentNode() {
        int rowIndex = control.getSelectedRow();
        if (controlModel.isReturner(rowIndex)) {
            return null;
        } else {
            return controlModel.getNodeAt(rowIndex);
        }
    }

    public void enterCurrentNode() {
        int rowIndex = control.getSelectedRow();
        if (rowIndex == -1) {
            return;
        }
        DvdKatModel coreModel = app.dvdKatModel;
        String path;
        NodeGeneric actNode;
        // is this a return to parent dir sign ("..")?
        if (controlModel.isReturner(rowIndex)) {
            gotoParentNode();
        } else {
            // otherwise get parent for the current node
            actNode = controlModel.getNodeAt(rowIndex);
            path = coreModel.getPathForNodeChilren(actNode);
            if (path != null) {
                populate(path);
            }
        }
    }

    public void gotoParentNode() {
        // if the first element in the table isn't a fake node (".."): return
        if (!controlModel.isReturner(0)) {
            return;
        }
        DvdKatModel coreModel = app.dvdKatModel;
        NodeGeneric firstRealNode = controlModel.getNodeAt(1);
        returnTo = coreModel.getParentNodeName(firstRealNode);
        String path = coreModel.getPathForParentNodeChildren(firstRealNode);
        if (path != null) {
            populate(path);
        }
    }

    public void gotoFirstNode() {
        toRow(0);
    }

    public void gotoLastNode() {
        int lastIdx = controlModel.getRowCount() - 1;
        toRow(lastIdx);
    }

    protected void toRow(int idx) {
        control.getSelectionModel().setSelectionInterval(idx, idx);
        Rectangle cellRect = control.getCellRect(idx, 0, true);
        control.scrollRectToVisible(cellRect);
    }

    public void dePopulate() {
        attribLister.setText("");
        controlModel.clear();
        control.revalidate();
        control.repaint();
    }

    public void disableAll() {
        control.setEnabled(false);
        control.setBackground(Color.getColor("Control"));
        attribLister.setEnabled(false);
    }

    public void enableAll() {
        control.setBackground(defaultColor);
        control.setEnabled(true);
        attribLister.setEnabled(true);
    }

    /**
     * populate the node table for a given abspath, then tries
     * to jump to the given name (row)
     * @param absPath   node's absolute path (id)
     * @param name  node name, used to jump on a node
     */
    void populateForAbsPath(String absPath, String name) {
        absPath = StringEscapeUtils.escapeXml(absPath); // this absPath may be coming from the searchView
        populate("//*[@absolutePath='" + absPath + "']/../*");
        //int idx = controlModel.getIdxForAbsPath(absPath);
        int idx = controlModel.getIdxForName(name);
        toRow(idx);
    }

}
