package org.garret.perst.continuous;

import org.garret.perst.*;
import org.apache.lucene.store.*;
import java.io.IOException;
import java.util.*;

public class PerstDirectory extends Directory
{    
    public PerstDirectory(Storage storage) { 
        delegate = new ByteBuffersDirectory();
    }

    public String[] listAll() throws IOException {
        return delegate.listAll();
    }

    public boolean fileExists(String name) throws IOException {
        return delegate.fileExists(name);
    }
    
    public long fileLength(String name) throws IOException {
        return delegate.fileLength(name);
    }

    public void deleteFile(String name) throws IOException {
        delegate.deleteFile(name);
    }

    public void rename(String source, String dest) throws IOException {
        delegate.rename(source, dest);
    }

    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return delegate.createOutput(name, context);
    }

    public IndexInput openInput(String name, IOContext context) throws IOException {
        return delegate.openInput(name, context);
    }

    public void close() throws IOException {
        delegate.close();
    }

    public void sync(Collection<String> names) throws IOException {
        delegate.sync(names);
    }

    public void syncMetaData() throws IOException {
        delegate.syncMetaData();
    }

    public Set<String> getPendingDeletions() throws IOException {
        return delegate.getPendingDeletions();
    }

    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        return delegate.createTempOutput(prefix, suffix, context);
    }

    public Lock obtainLock(String name) throws IOException {
        return delegate.obtainLock(name);
    }

    private final ByteBuffersDirectory delegate;
}
