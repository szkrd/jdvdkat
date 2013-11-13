package com.rosamez.dirlistex;

import java.io.IOException;
import java.util.ArrayList;

public interface ArchiveReader {
	
	ArrayList<ArchivedFile> getListOfFiles() throws IOException;
	
}
