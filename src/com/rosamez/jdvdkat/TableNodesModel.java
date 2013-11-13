/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.nodes.NodeGeneric;
import com.rosamez.jdvdkat.nodes.NodeDirectory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Szabolcs Kurdi
 */
public class TableNodesModel extends AbstractTableModel {

    protected String[] columnNames = {"Name", "Size", "Type", "Date"};
    protected Object[][] data = null;
    protected ArrayList storedNodes = null;
    private Boolean hasReturner = false;
    protected SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd. kk:mm");
    protected Boolean directoriesFirst = true;
    private int[] translatorMap;

    public void setData(ArrayList al, Boolean allowReturner) {
        hasReturner = false;
        storedNodes = al;
        int alSize = al.size();
        int rowLength = alSize;
        int offset = 0;

        // if one node has a parent, than all must have
        if (alSize > 0) {
            NodeGeneric firstItem = (NodeGeneric) al.get(0);
            if (firstItem.hasParent && allowReturner) {
                hasReturner = true;
                rowLength++;
                offset++;
            }
        }

        data = new Object[rowLength][4];
        translatorMap = new int[rowLength]; // TODO: do I really want this?
        if (hasReturner) {
            data[0][0] = "..";
            data[0][1] = "";
            data[0][2] = "directory";
        }

        int dirCount = 0;
        if (directoriesFirst) {
            for (int i = 0; i < al.size(); i++) {
                if ((NodeGeneric) al.get(i) instanceof NodeDirectory) {
                    dirCount++;
                }
            }
        }

        int loc = 0;
        int tempDirCount = 0;
        int tempNodeCount = 0;
        for (int i = 0; i < al.size(); i++) {
            NodeGeneric node = (NodeGeneric) al.get(i);
            loc = i;

            if (directoriesFirst) {
                if (node instanceof NodeDirectory) {
                    loc = tempDirCount;
                    tempDirCount++;
                } else {
                    loc = dirCount + tempNodeCount;
                    tempNodeCount++;
                }
            }

            translatorMap[loc] = i;
            createRow(loc + offset, node);
        }
    }

    protected void createRow(int rowNum, NodeGeneric node) {
        data[rowNum][0] = node.name;
        data[rowNum][1] = node.getFormattedSize();
        data[rowNum][2] = node.getPrettyType();
        data[rowNum][3] = node.getLastModDate(dateFormatter);
    }

    public void setData(Object[][] data) {
        this.data = data;
    }

    public void clear() {
        data = new Object[0][0];
        storedNodes = new ArrayList();
    }

    public Boolean isReturner(int idx) {
        return ((idx == 0) && (hasReturner));
    }

    public NodeGeneric getNodeAt(int idx) {
        int offsetedIdx = idx - (hasReturner ? 1 : 0);
        if (offsetedIdx < 0) { // TODO: why do we need this?
            return null;
        }
        if (storedNodes.size() < offsetedIdx) {
            return null;
        }
        offsetedIdx = translatorMap[offsetedIdx];
        return (NodeGeneric) storedNodes.get(offsetedIdx);
    }

    public String[] getColumns() {
        return columnNames;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public int getIdxForName(String nodeName) {
        for (int i = 0; i < data.length; i++) {
            if (nodeName.equals(data[i][0])) {
                return i;
            }
        }
        return 0;
    }

    public int getIdxForAbsPath(String absPath) {
        for (int i = 0; i < data.length; i++) {
            NodeGeneric node = getNodeAt(i);
            if (node != null) {
                String nodeAP = node.absolutePath; // really shouldn't be null...
                if (nodeAP != null && absPath.equals(nodeAP)) {
                    return i;
                }
            }
        }
        return 0;
    }
}
