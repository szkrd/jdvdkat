package com.rosamez.dirlistex;

import java.io.File;

public class SimpleFileUtils {

	/**
	 * return filename suffix part
	 * @param fileName
	 * @return
	 */
	public static String getSuffix(String fileName) {
                String name = fileName;
                String sfx = null;
		int extDotPos;
                int extSlashPos = name.lastIndexOf("/");

                // inside archives we may have directory separators
                if ((extSlashPos > -1)) { // && (extSlashPos < fileName.length() - 1)
                    name = name.substring(extSlashPos + 1);
                }

                // and now the suffix part
                extDotPos = name.lastIndexOf(".");
		if (extDotPos > 0) { // not -1!
                    sfx = name.substring(extDotPos + 1).toLowerCase();
		}

                return sfx;
	}
	
	public static String getSuffix(File file) {
		return getSuffix(file.getName());
	}

        public static String sanitizePath(String path) {
            return path.replaceAll("\\\\", "/");
        }
	
}
