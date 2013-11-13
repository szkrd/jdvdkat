package com.rosamez.dirlistex;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

public class PictureFile {
	public int width = -1;
	public int height = -1;

	public PictureFile(File file) throws IOException {
		try {
			// I really don't need these, but who knows...
			//ImageInfo ii = Sanselan.getImageInfo(file);
			//width = ii.getWidth();
			//height = ii.getHeight();
			//bpp = ii.getBitsPerPixel();
			// etc.
			
			Dimension d = Sanselan.getImageSize(file);
			height = (int)d.getHeight();
			width = (int)d.getWidth();
		} catch (ImageReadException e) {
			throw new IOException();
		}		
	}	
}
