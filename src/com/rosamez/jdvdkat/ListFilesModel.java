package com.rosamez.jdvdkat;

import javax.swing.DefaultListModel;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Szabolcs Kurdi
 */
public class ListFilesModel extends DefaultListModel {

    public int getIdxForFileName(String absolutePath) {
        String fileName = FilenameUtils.getName(absolutePath);
        for (int i = 0; i < this.size(); i++) {
            if (fileName.equals((String) this.get(i))) {
                return i;
            }
        }
        return 0;
    }

}
