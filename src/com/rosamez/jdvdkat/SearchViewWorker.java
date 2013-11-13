/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rosamez.jdvdkat;

import javax.swing.SwingWorker;

/**
 *
 * @author Szabolcs Kurdi
 */
class SearchViewWorker extends SwingWorker {
    private SearchTableNodesController searchController;
    private String fileName;
    private String path;

    /**
     * sets up the worker for searching
     * @param searchController
     * @param fileName  search parameter
     * @param path  query
     */
    public SearchViewWorker(SearchTableNodesController searchController, String fileName, String path) {
        this.searchController = searchController;
        this.fileName = fileName;
        this.path = path;
    }

    @Override
    protected Object doInBackground() throws Exception {
        Integer count = searchController.populate(fileName, path);
        this.firePropertyChange("done", null, count);
        return null;
    }

}
