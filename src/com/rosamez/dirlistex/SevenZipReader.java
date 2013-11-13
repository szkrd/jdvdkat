package com.rosamez.dirlistex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
// J7Zip - from J7zip_4.43_alpha2.zip
// http://sourceforge.net/projects/p7zip/files/
import SevenZip.Archive.SevenZip.Handler;
import SevenZip.Archive.SevenZipEntry;
import SevenZip.Archive.IInArchive;

public class SevenZipReader extends ZipReader {

	public SevenZipReader(File file) {
		super(file);
	}

	/**
	 * Based on the code from J7Zip.java, the example code
	 * for J7Zip 4.43 alpha 2
	 */
	public ArrayList<ArchivedFile> getListOfFiles() throws IOException {
		
		SevenZipRandomAccessHelper istream = new SevenZipRandomAccessHelper(file.getAbsolutePath(), "r");
		IInArchive archive = new Handler();
		int ret = archive.Open(istream);
		
		if (ret != 0) {
            throw new IOException();
        }
		
		ArrayList<ArchivedFile> items = new ArrayList<ArchivedFile>();
		
		for (int i = 0; i < archive.size(); i++) {
			SevenZipEntry sZItem = archive.getEntry(i);
			ArchivedFile item = new ArchivedFile();
			
			item.size = sZItem.getSize();
			item.name = sZItem.getName();
			item.date = sZItem.getTime();
			//System.out.println(String.format("Archived Entry: %s len %d added %d", item.name, item.size, item.date));
			items.add(item);
		}
		
		return items;
	}
	
}
