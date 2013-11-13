/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import com.rosamez.jdvdkat.nodes.NodeGeneric;
import java.util.ArrayList;

/**
 *
 * @author Szabolcs Kurdi
 */
class SearchTableNodesModel extends TableNodesModel {

    public SearchTableNodesModel() {
        columnNames = new String[]{"File", "Name", "Path", "Size", "Type", "Date"};
    }

    @Override
    public void setData(ArrayList al, Boolean allowReturner) {
        storedNodes = al;
        int aS = al.size();
        data = new Object[aS][columnNames.length];
        for (int i = 0; i < aS; i++) {
            createRow(i, (NodeGeneric) al.get(i));
        }
    }

    @Override
    protected void createRow(int rowNum, NodeGeneric node) {
        data[rowNum][0] = node.getShortDoc();
        data[rowNum][1] = node.name;
        data[rowNum][2] = node.getTruncAbsPath();
        data[rowNum][3] = node.size;
        data[rowNum][4] = node.getPrettyType();
        data[rowNum][5] = node.dateNative;
    }

    @Override
    public NodeGeneric getNodeAt(int idx) {
        return (NodeGeneric) storedNodes.get(idx);
    }
}
