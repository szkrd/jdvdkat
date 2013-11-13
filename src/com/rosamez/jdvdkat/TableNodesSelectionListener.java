/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * ugly callback for rowSelectionChange
 * @author Szabolcs Kurdi
 */
public class TableNodesSelectionListener implements ListSelectionListener {
    private TableNodesController callbackObj;

    public TableNodesSelectionListener(TableNodesController callbackObj) {
        this.callbackObj = callbackObj;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        // the mouse button has not yet been released
        if (e.getValueIsAdjusting()) {
            return;
        }

        callbackObj.onRowSelectionChange();
    }
}
