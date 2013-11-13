package com.rosamez.dirlistex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipReader implements ArchiveReader {

    protected File file;

    public ZipReader(File file) {
        this.file = file;
    }

    // TODO: fix exception handling
    public ArrayList<ArchivedFile> getListOfFiles() throws IOException {

        InputStream theZipFile;
        try {
            theZipFile = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IOException();
        }
        ZipInputStream stream = new ZipInputStream(theZipFile);

        ArrayList<ArchivedFile> items = new ArrayList<ArchivedFile>();
        try {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                ArchivedFile item = new ArchivedFile();
                item.name = entry.getName();
                item.size = entry.getSize();
                item.date = entry.getTime();
                //System.out.println(String.format("Archived Entry: %s len %d added %d", item.name, item.size, item.date));
                items.add(item);
            }
        } finally {
            stream.close();
        }

        return items;
    }
}
