package com.rosamez.dirlistex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Reads DOS descript.ion files (ISO-8859-1), stores
 * all filenames and comments in filesAndComments hashmap
 */
public class DescriptIonParser {
	protected HashMap<String, String> filesAndComments = new HashMap<String, String>();
	protected ArrayList<String> parsedDionFiles = new ArrayList<String>();

	public DescriptIonParser() {
		
	}
	
	/**
	 * Searches for the descript.ion file in a given directory,
	 * if it finds it then parses it into the filesAndComments hashmap
	 * @param file	directory
	 * @throws IOException	throws it if the descript.ion exists, but could not be parsed
	 */
	public void testDirectory(File file) throws IOException {
		if (!file.isDirectory()) {
			return;
		}
		
		String dIonAbsPath = file.getAbsolutePath() + File.separator + "descript.ion";
		File dIonFile = new File(dIonAbsPath);
		
		// bail out if descript.ion doesn't exist
		if (!dIonFile.exists())
			return;
		
		// we have already parsed this dion file
		if (parsedDionFiles.contains(parsedDionFiles))
			return;
		
		parsedDionFiles.add(dIonAbsPath);
		
		try {
			FileInputStream fr = new FileInputStream(dIonFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fr, "ISO-8859-1"));
			
			String line = "";
			String fullFileName = "";
			String fileNamePart = "";
			String commentPart = "";
			int loc = 0;
			while((line = br.readLine()) != null) {
				if (line.trim().length() > 0) {
					if (line.substring(0, 1).equals("\"")) {
						
						// filename was in quotes (had spaces)
						
						loc = line.indexOf("\"", 1);
						if (loc > -1) {
							fileNamePart = line.substring(1, loc);
							commentPart = line.substring(loc + 1).trim();
						}
					} else {
						
						// filename has no spaces
						
						loc = line.indexOf(" ");
						if (loc > -1) {
							fileNamePart = line.substring(0, loc);
							commentPart = line.substring(loc).trim();
						}
					}
					
					if ((!fileNamePart.equals("")) && (!commentPart.equals(""))) {
						fullFileName = file.getAbsolutePath() + File.separator + fileNamePart;
						
						filesAndComments.put(fullFileName, commentPart);
						//System.out.println(fullFileName + " -----> " + commentPart);
					}

				}
			}
			
		} catch (Exception e) {
			throw new IOException();
		}
		
	}
	
	/**
	 * Checks if a file's absolute path has a comment attached to it
	 * @param file
	 * @return
	 */
	public boolean hasDIonComments(File file) {
		return filesAndComments.containsKey(file.getAbsolutePath());
	}
	
	/**
	 * Returns the comments for a given file
	 * @param file
	 * @return
	 */
	public String getCommentsFor(File file) {
		return filesAndComments.get(file.getAbsolutePath());
	}

}
