package com.rosamez.dirlistex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.innosystec.unrar.*;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import java.io.FileInputStream;

public class RarReader extends ZipReader {

    public RarReader(File file) {
        super(file);
    }

    @Override
    public ArrayList<ArchivedFile> getListOfFiles() throws IOException {

        // I check the rar header myself, ffs
        FileInputStream f = new FileInputStream(this.file);
        byte[] b = new byte[4];
        f.read(b);
        String rarHeader = new String(b);
        f.close();
        if (!"Rar!".equals(rarHeader)) {
            throw new IOException();
        }

        // de.innosystec.unrar is a piece of shit
        ArrayList<ArchivedFile> items = new ArrayList<ArchivedFile>();
        try {
            Archive rarFile = new Archive(file);
            List<FileHeader> headers = rarFile.getFileHeaders();
            for (FileHeader fileHeader : headers) {
                ArchivedFile item = new ArchivedFile();
                item.name = fileHeader.getFileNameString();
                item.date = fileHeader.getMTime().getTime();
                item.size = fileHeader.getUnpSize();
                //System.out.println(String.format("Archived Entry: %s len %d added %d", item.name, item.size, item.date));
                items.add(item);
            }

        } catch (Exception e) {
            throw new IOException();
            //System.err.println("rar reading error");
            //e.printStackTrace();
        }

        return items;
    }
}
