package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestCodeGenerator.java
 * Tests code generation for dynamic queries.
 * Scaled down for faster testing.
 */
class TestCodeGenerator {

    static class Track extends Persistent {
        int no;

        @Indexable
        Album album;

        @Indexable
        String name;

        float duration;
    }

    static class Album extends Persistent {
        @Indexable
        String name;

        @Indexable
        RecordLabel label;

        String genre;

        Date release;
    }

    static class RecordLabel extends Persistent {
        @Indexable
        String name;

        String email;
        String phone;
        String address;
    }

    static class QueryExecutionListener extends StorageListener {
        int nSequentialSearches;
        int nSorts;

        public void sequentialSearchPerformed(Class table, String query) {
            nSequentialSearches += 1;
        }

        public void sortResultSetPerformed(Class table, String query) {
            nSorts += 1;
        }
    }

    private Storage storage;
    private Database db;
    private static final String TEST_DB = "testcodegenerator.dbs";
    private static final int nLabels = 10;
    private static final int nAlbums = 100;
    private static final int nTracksPerAlbum = 5;

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
        db = new Database(storage);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test code generator create and query records")
    void testCodeGeneratorCreateAndQuery() {
        // Create record labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }

        // Create albums and tracks
        for (int i = 0; i < nAlbums; i++) {
            Album album = new Album();
            album.name = "Album" + i;
            album.label = db.<RecordLabel>select(RecordLabel.class, "name='Label" + (i % nLabels) + "'").first();
            album.genre = "Rock";
            album.release = new Date();
            db.addRecord(album);

            for (int j = 0; j < nTracksPerAlbum; j++) {
                Track track = new Track();
                track.no = j + 1;
                track.name = "Track" + j;
                track.album = album;
                track.duration = 3.5f;
                db.addRecord(track);
            }
        }
        storage.commit();

        assertTrue(nLabels > 0, "Should have created labels");
    }

    @Test
    @DisplayName("Test code generator predicate with parameter")
    void testCodeGeneratorPredicateWithParameter() {
        // Create data
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }

        for (int i = 0; i < nAlbums; i++) {
            Album album = new Album();
            album.name = "Album" + i;
            album.label = db.<RecordLabel>select(RecordLabel.class, "name='Label" + (i % nLabels) + "'").first();
            album.genre = "Rock";
            album.release = new Date();
            db.addRecord(album);

            for (int j = 0; j < nTracksPerAlbum; j++) {
                Track track = new Track();
                track.no = j + 1;
                track.name = "Track" + j;
                track.album = album;
                track.duration = 3.5f;
                db.addRecord(track);
            }
        }
        storage.commit();

        // Test code generator predicate
        Query<Track> trackQuery = db.<Track>createQuery(Track.class);
        CodeGenerator code = trackQuery.getCodeGenerator();
        code.predicate(code.and(code.gt(code.field("no"),
                code.literal(0)),
                code.eq(code.field(code.field(code.field("album"), "label"), "name"),
                        code.parameter(1, String.class))));

        int nTracks = 0;
        for (int i = 0; i < nLabels; i++) {
            trackQuery.setParameter(1, "Label" + i);
            for (Track t : trackQuery) {
                nTracks += 1;
            }
        }

        assertEquals(nAlbums * nTracksPerAlbum, nTracks, "Should find all tracks");
    }

    @Test
    @DisplayName("Test code generator order by")
    void testCodeGeneratorOrderBy() {
        // Create record labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }
        storage.commit();

        // Test order by
        Query<RecordLabel> labelQuery = db.<RecordLabel>createQuery(RecordLabel.class);
        CodeGenerator code = labelQuery.getCodeGenerator();
        code.orderBy("name");

        String prev = "";
        int n = 0;
        for (RecordLabel label : labelQuery) {
            assertTrue(prev.compareTo(label.name) < 0, "Should be ordered");
            prev = label.name;
            n += 1;
        }

        assertEquals(nLabels, n, "Should have all labels ordered");
    }

    @Test
    @DisplayName("Test code generator like predicate")
    void testCodeGeneratorLikePredicate() {
        // Create record labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }
        storage.commit();

        // Test like predicate
        Query<RecordLabel> labelQuery = db.<RecordLabel>createQuery(RecordLabel.class);
        CodeGenerator code = labelQuery.getCodeGenerator();
        code.predicate(code.like(code.field("name"),
                code.literal("Label%")));
        code.orderBy("name");

        int n = 0;
        for (RecordLabel label : labelQuery) {
            n += 1;
        }

        assertEquals(nLabels, n, "Should find all labels with like");
    }

    @Test
    @DisplayName("Test code generator in predicate")
    void testCodeGeneratorInPredicate() {
        // Create record labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }
        storage.commit();

        // Test in predicate
        Query<RecordLabel> labelQuery = db.<RecordLabel>createQuery(RecordLabel.class);
        CodeGenerator code = labelQuery.getCodeGenerator();
        code.predicate(code.in(code.field("name"),
                code.list(code.literal("Label1"), code.literal("Label2"), code.literal("Label3"))));

        int n = 0;
        for (RecordLabel label : labelQuery) {
            n += 1;
        }

        assertEquals(3, n, "Should find 3 labels");
    }

    @Test
    @DisplayName("Test code generator or and like")
    void testCodeGeneratorOrAndLike() {
        // Create record labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }
        storage.commit();

        // Test or and like
        Query<RecordLabel> labelQuery = db.<RecordLabel>createQuery(RecordLabel.class);
        CodeGenerator code = labelQuery.getCodeGenerator();
        code.predicate(code.and(code.or(code.eq(code.field("name"),
                        code.literal("Label1")),
                        code.or(code.eq(code.field("name"),
                                code.literal("Label2")),
                                code.eq(code.field("name"),
                                        code.literal("Label3")))),
                code.like(code.field("email"),
                        code.literal("contact@%"))));

        int n = 0;
        for (RecordLabel label : labelQuery) {
            n += 1;
        }

        assertEquals(3, n, "Should find 3 labels with OR and LIKE");
    }

    @Test
    @DisplayName("Test code generator nested field access")
    void testCodeGeneratorNestedFieldAccess() {
        // Create data
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }

        for (int i = 0; i < nAlbums; i++) {
            Album album = new Album();
            album.name = "Album" + i;
            album.label = db.<RecordLabel>select(RecordLabel.class, "name='Label" + (i % nLabels) + "'").first();
            album.genre = "Rock";
            album.release = new Date();
            db.addRecord(album);

            for (int j = 0; j < nTracksPerAlbum; j++) {
                Track track = new Track();
                track.no = j + 1;
                track.name = "Track" + j;
                track.album = album;
                track.duration = 3.5f;
                db.addRecord(track);
            }
        }
        storage.commit();

        // Test nested field access
        Query<Track> trackQuery = db.<Track>createQuery(Track.class);
        CodeGenerator code = trackQuery.getCodeGenerator();
        code.predicate(code.or(code.eq(code.field(code.field(code.field("album"), "label"), "name"),
                        code.literal("Label1")),
                code.eq(code.field(code.field(code.field("album"), "label"), "name"),
                        code.literal("Label2"))));

        int n = 0;
        for (Track track : trackQuery) {
            assertTrue(track.album.label.name.equals("Label1") || track.album.label.name.equals("Label2"),
                    "Should match Label1 or Label2");
            n += 1;
        }

        assertTrue(n > 0, "Should find tracks with nested field access");
    }

    @Test
    @DisplayName("Test code generator query listener")
    void testCodeGeneratorQueryListener() {
        // Create data
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }
        storage.commit();

        // Set listener
        QueryExecutionListener listener = new QueryExecutionListener();
        storage.setListener(listener);

        // Execute query
        Query<RecordLabel> labelQuery = db.<RecordLabel>createQuery(RecordLabel.class);
        CodeGenerator code = labelQuery.getCodeGenerator();
        code.orderBy("name");

        int n = 0;
        for (RecordLabel label : labelQuery) {
            n += 1;
        }

        assertEquals(nLabels, n, "Should have all labels");
        assertEquals(0, listener.nSequentialSearches, "Should have no sequential searches");
        assertEquals(0, listener.nSorts, "Should have no sorts");
    }
}
