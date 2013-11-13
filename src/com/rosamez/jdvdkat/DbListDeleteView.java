/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rosamez.jdvdkat;

import java.awt.Frame;
import java.awt.event.KeyEvent;

/**
 * Same view as dbList, but the action button's text is set to "Delete"
 * @author Szabolcs Kurdi
 */
public class DbListDeleteView extends DbListView {

    public DbListDeleteView(Frame parent, boolean modal) {
        super(parent, modal);
    }

    @Override
    public void init() {
        super.init();
        setupActionButton("Delete", KeyEvent.VK_D);
    }

}
