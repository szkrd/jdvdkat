/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosamez.jdvdkat;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

/**
 *
 * @author Szabolcs Kurdi
 */
public class AddAllFilesView extends ProgressBarDialog implements PropertyChangeListener {
    private int succAdds = 0;
    private AddAllFilesWorker task;
    private int numFiles = 0;
    private static Logger log = Logger.getLogger(AddAllFilesView.class);

    /**
     * set the title, call the parent progressbar dialog constructor
     * @param parent
     * @param modal
     */
    public AddAllFilesView(Frame parent, boolean modal) {
        super(parent, modal);
        this.setTitle("Add all");
    }

    /**
     * show the form and start the import process
     * @param callerPanel
     * @param dir   directory with xml files
     */
    public void doImportShow(JPanel callerPanel, File dir) {
        this.setLocationRelativeTo(callerPanel);

        // get number of xml files
        FilenameFilter filefilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        };
        File[] files = dir.listFiles(filefilter);
        numFiles = files.length;

        // bail out if no files found
        if (numFiles == 0) {
            JOptionPane.showMessageDialog(parentPanel, "No XML files found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // otherwise show and reset form
        setMessage("Importing " + numFiles + " file(s)...");
        setProgressMax(numFiles);
        runModal();

        disableActionButton();
        succAdds = 0;

        // start bg task
        task = new AddAllFilesWorker(app.dbManager, files);
        task.addPropertyChangeListener((PropertyChangeListener) this);
        task.execute();
    }

    /**
     * on setprogress or explicit property change event:
     * update the progressbar + message if finished
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        Integer propNewVal = evt.getNewValue() instanceof Integer ? (Integer) evt.getNewValue() : null;
        int percent = task.getProgress(); // hell, it's in percentage
        if ("done".equals(propName)) {
            succAdds = propNewVal;
            setProgressVal(numFiles);
            if (succAdds == numFiles) {
                setMessage("Imported all XML files.");
            } else {
                setMessage("Warning! Imported " + succAdds + " file(s) only (out of " + numFiles + ").");
            }
            enableActionButton();
        } else {
            if (!task.isDone()) {
                setProgressVal(numFiles * percent / 100);
            }
        }
    }

    public int getLastSuccAdds() {
        return succAdds;
    }

    @Override
    protected void onClose() {
        app.mainWindow.onAddAllFilesFinished();
    }
}
