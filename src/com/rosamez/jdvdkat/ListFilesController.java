package com.rosamez.jdvdkat;

import java.awt.Color;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Szabolcs Kurdi
 */
class ListFilesController {
    private JList control;
    private Color defaultColor;
    private ListFilesModel controlModel = new ListFilesModel();

    public ListFilesController(JList control) {
        this.control = control;
        control.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        control.setModel(controlModel);
        defaultColor = control.getBackground();
    }

    public void clear() {
        controlModel.clear();
    }

    public void dePopulate() {
        clear();
    }

    public void add(String text) {
        controlModel.add(controlModel.size(), text);
    }

    public void gotoFirst() {
        control.setSelectedIndex(0);
    }

    public void gotoFileName(String path) {
        control.setSelectedIndex(controlModel.getIdxForFileName(path));
    }

    public boolean isEmpty() {
        return controlModel.getSize() == 0;
    }

    public String getCurrent() {
        return (String)control.getSelectedValue();
    }

    public void gotoItem(String s) {
        control.setSelectedValue(s, true);
    }

    public void disable() {
        control.setBackground(Color.getColor("Control"));
        control.setEnabled(false);
    }

    public void enable() {
        control.setBackground(defaultColor);
        control.setEnabled(true);
    }

}
