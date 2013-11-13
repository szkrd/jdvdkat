package com.rosamez.dirlistex;

import java.io.RandomAccessFile;

/**
 * Copy paste from MyRandomAccessFile (in j7zip's examples directory), 
 * where the constructor was not visible.
 * J7Zip is part of p7Zip at http://sourceforge.net/projects/p7zip/files/
 */
public class SevenZipRandomAccessHelper extends SevenZip.IInStream  {
    
    protected RandomAccessFile _file;
    
    public SevenZipRandomAccessHelper(String filename,String mode) throws java.io.IOException {
        _file = new java.io.RandomAccessFile(filename,mode);
    }
    
    public long Seek(long offset, int seekOrigin) throws java.io.IOException {
        if (seekOrigin == STREAM_SEEK_SET) {
            _file.seek(offset);
        }
        else if (seekOrigin == STREAM_SEEK_CUR) {
            _file.seek(offset + _file.getFilePointer());
        }
        return _file.getFilePointer();
    }
    
    public int read() throws java.io.IOException {
        return _file.read();
    }
 
    public int read(byte [] data, int off, int size) throws java.io.IOException {
        return _file.read(data,off,size);
    }
        
    public int read(byte [] data, int size) throws java.io.IOException {
        return _file.read(data,0,size);
    }
    
    public void close() throws java.io.IOException {
        _file.close();
        _file = null;
    }   
}
