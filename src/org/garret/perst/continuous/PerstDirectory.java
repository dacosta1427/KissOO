package org.garret.perst.continuous;

import org.garret.perst.*;
import org.apache.lucene.store.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @deprecated PerstDirectory is temporarily disabled for Lucene 9.x compatibility.
 * The in-Perst Lucene index storage feature is not available.
 * Use FSDirectory (file system based Lucene index) instead.
 */
@Deprecated
abstract class PerstDirectory extends Directory 
{    
    public PerstDirectory(RootObject root) { 
        throw new UnsupportedOperationException(
            "PerstDirectory is temporarily disabled for Lucene 9.x compatibility. " +
            "Please use file system based Lucene index (provide a path to CDatabase.open()).");
    }

    public String[] list() throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean fileExists(String name) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public long fileModified(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void touchFile(String name) throws IOException {
        throw new UnsupportedOperationException();
    }
        
    public void deleteFile(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void renameFile(String from, String to) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void rename(String from, String to) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    public IndexInput openInput(String name, IOContext context) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Lock obtainLock(String name) {
        throw new UnsupportedOperationException();
    }

    public Set<String> getPendingDeletions() {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
}
