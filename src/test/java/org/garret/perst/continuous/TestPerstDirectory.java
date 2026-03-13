package org.garret.perst.continuous;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class TestPerstDirectory {
    
    @AfterEach
    public void tearDown() throws IOException {
        if (delegate != null) {
            delegate.close();
        }
    }

    @Test
    public void testCreateAndSearchIndex() throws IOException {
        delegate = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(delegate, config);

        Document doc = new Document();
        doc.add(new TextField("title", "Java Programming", Field.Store.YES));
        doc.add(new TextField("content", "Learn Java from basics to advanced", Field.Store.YES));
        writer.addDocument(doc);

        Document doc2 = new Document();
        doc2.add(new TextField("title", "Python Tutorial", Field.Store.YES));
        doc2.add(new TextField("content", "Learn Python programming", Field.Store.YES));
        writer.addDocument(doc2);

        writer.commit();
        writer.close();

        DirectoryReader reader = DirectoryReader.open(delegate);
        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery query = new TermQuery(new Term("title", "java"));
        TopDocs results = searcher.search(query, 10);

        assertEquals(1, results.totalHits.value);

        reader.close();
    }

    @Test
    public void testMultipleDocuments() throws IOException {
        delegate = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(delegate, config);

        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            doc.add(new TextField("id", String.valueOf(i), Field.Store.YES));
            doc.add(new TextField("content", "Document number " + i, Field.Store.YES));
            writer.addDocument(doc);
        }

        writer.commit();
        writer.close();

        DirectoryReader reader = DirectoryReader.open(delegate);
        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery query = new TermQuery(new Term("content", "document"));
        TopDocs results = searcher.search(query, 100);

        assertEquals(100, results.totalHits.value);

        reader.close();
    }

    @Test
    public void testUpdateDocument() throws IOException {
        delegate = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(delegate, config);

        Document doc = new Document();
        doc.add(new TextField("title", "Original Title", Field.Store.YES));
        doc.add(new StringField("id", "1", Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        writer.close();

        IndexWriterConfig config2 = new IndexWriterConfig(analyzer);
        IndexWriter writer2 = new IndexWriter(delegate, config2);

        Document doc2 = new Document();
        doc2.add(new TextField("title", "Updated Title", Field.Store.YES));
        doc2.add(new StringField("id", "1", Field.Store.YES));
        writer2.updateDocument(new Term("id", "1"), doc2);
        writer2.commit();
        writer2.close();

        DirectoryReader reader = DirectoryReader.open(delegate);
        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery query = new TermQuery(new Term("id", "1"));
        TopDocs results = searcher.search(query, 10);

        assertEquals(1, results.totalHits.value);
        assertEquals("Updated Title", searcher.doc(results.scoreDocs[0].doc).get("title"));

        reader.close();
    }

    @Test
    public void testDeleteDocument() throws IOException {
        delegate = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(delegate, config);

        Document doc = new Document();
        doc.add(new TextField("title", "To Be Deleted", Field.Store.YES));
        doc.add(new StringField("id", "1", Field.Store.YES));
        writer.addDocument(doc);

        Document doc2 = new Document();
        doc2.add(new TextField("title", "To Keep", Field.Store.YES));
        doc2.add(new StringField("id", "2", Field.Store.YES));
        writer.addDocument(doc2);

        writer.commit();
        writer.close();

        IndexWriterConfig config2 = new IndexWriterConfig(analyzer);
        IndexWriter writer2 = new IndexWriter(delegate, config2);
        writer2.deleteDocuments(new Term("id", "1"));
        writer2.commit();
        writer2.close();

        DirectoryReader reader = DirectoryReader.open(delegate);
        IndexSearcher searcher = new IndexSearcher(reader);

        TermQuery query = new TermQuery(new Term("title", "to"));
        TopDocs results = searcher.search(query, 10);

        assertEquals(1, results.totalHits.value);
        assertEquals("To Keep", searcher.doc(results.scoreDocs[0].doc).get("title"));

        reader.close();
    }

    @Test
    public void testDirectoryList() throws IOException {
        delegate = new ByteBuffersDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(delegate, config);

        for (int i = 0; i < 5; i++) {
            Document doc = new Document();
            doc.add(new TextField("content", "doc" + i, Field.Store.YES));
            writer.addDocument(doc);
        }

        writer.commit();
        writer.close();

        String[] files = delegate.listAll();
        assertTrue(files.length > 0);
    }

    private Directory delegate;
}
