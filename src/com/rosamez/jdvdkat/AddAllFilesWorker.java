/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import java.io.File;
import javax.swing.SwingWorker;

/**
 *
 * @author Szabolcs Kurdi
 */
public class AddAllFilesWorker extends SwingWorker {
    private File[] xmlFiles;
    private DbManager dbManager;

    public AddAllFilesWorker(DbManager dbManager, File[] xmlFiles) {
        this.xmlFiles = xmlFiles;
        this.dbManager = dbManager;
    }

    @Override
    public Void doInBackground() {
        Integer succImp = 0;
        int len = xmlFiles.length;
        for (int i = 0; i < len; i++) {
            File file = xmlFiles[i];
            if (dbManager.addFileNoOpt(file)) {
                setProgress(i * 100 / len);
                succImp++;
            }
        }
        dbManager.optimize();
        this.firePropertyChange("done", null, succImp);
        return null;
    }
}
