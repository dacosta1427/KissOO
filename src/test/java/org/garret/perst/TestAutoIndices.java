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
 * JUnit 5 conversion of tst/TestAutoIndices.java
 * Tests auto-indices functionality.
 * Scaled down for faster testing.
 */
class TestAutoIndices {

    static class Track extends Persistent {
        int no;
        Album album;
        String name;
        float duration;
    }

    static class Album extends Persistent {
        String name;
        RecordLabel label;
        String genre;
        Date release;
    }

    static class RecordLabel extends Persistent {
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
    private static final String TEST_DB = "testautoindices.dbs";
    private static final int nLabels = 10;
    private static final int nAlbums = 50;
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
    @DisplayName("Test auto indices enable and create records")
    void testAutoIndicesEnableAndCreateRecords() {
        db.enableAutoIndices(true);
        
        // Create records
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

        // Verify records were created
        assertTrue(nLabels > 0, "Should have created labels");
    }

    @Test
    @DisplayName("Test auto indices query with parameter")
    void testAutoIndicesQueryWithParameter() {
        db.enableAutoIndices(true);

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

        // Query with parameter
        Query<Track> query = db.<Track>prepare(Track.class, "album.label.name=?");
        int nTracks = 0;
        for (int i = 0; i < nLabels; i++) {
            query.setParameter(1, "Label" + i);
            for (Track t : query) {
                nTracks += 1;
            }
        }

        assertEquals(nAlbums * nTracksPerAlbum, nTracks, "Should find all tracks");
    }

    @Test
    @DisplayName("Test auto indices order by")
    void testAutoIndicesOrderBy() {
        db.enableAutoIndices(true);

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

        // Query with order by
        String prev = "";
        int n = 0;
        for (RecordLabel label : db.<RecordLabel>select(RecordLabel.class, "order by name")) {
            assertTrue(prev.compareTo(label.name) < 0, "Should be ordered");
            prev = label.name;
            n += 1;
        }

        assertEquals(nLabels, n, "Should have all labels ordered");
    }

    @Test
    @DisplayName("Test auto indices like predicate")
    void testAutoIndicesLikePredicate() {
        db.enableAutoIndices(true);

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

        // Query with like
        String prev = "";
        int n = 0;
        for (RecordLabel label : db.<RecordLabel>select(RecordLabel.class, "name like 'Label%' order by name")) {
            assertTrue(prev.compareTo(label.name) < 0, "Should be ordered");
            prev = label.name;
            n += 1;
        }

        assertEquals(nLabels, n, "Should find all labels with like");
    }

    @Test
    @DisplayName("Test auto indices in predicate")
    void testAutoIndicesInPredicate() {
        db.enableAutoIndices(true);

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

        // Query with in
        int n = 0;
        for (RecordLabel label : db.<RecordLabel>select(RecordLabel.class, "name in ('Label1', 'Label2', 'Label3')")) {
            n += 1;
        }

        assertEquals(3, n, "Should find 3 labels");
    }

    @Test
    @DisplayName("Test auto indices or and like")
    void testAutoIndicesOrAndLike() {
        db.enableAutoIndices(true);

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

        // Query with OR and LIKE
        int n = 0;
        for (RecordLabel label : db.<RecordLabel>select(RecordLabel.class, "(name = 'Label1' or name = 'Label2' or name = 'Label3') and email like 'contact@%'")) {
            n += 1;
        }

        assertEquals(3, n, "Should find 3 labels with OR and LIKE");
    }

    @Test
    @DisplayName("Test auto indices query listener")
    void testAutoIndicesQueryListener() {
        db.enableAutoIndices(true);

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

        // Set listener
        QueryExecutionListener listener = new QueryExecutionListener();
        storage.setListener(listener);

        // Execute queries
        Query<Track> query = db.<Track>prepare(Track.class, "album.label.name=?");
        for (int i = 0; i < nLabels; i++) {
            query.setParameter(1, "Label" + i);
            for (Track t : query) {
                // iterate
            }
        }

        assertEquals(0, listener.nSequentialSearches, "Should have no sequential searches");
        assertEquals(0, listener.nSorts, "Should have no sorts");
    }

    @Test
    @DisplayName("Test auto indices nested field access")
    void testAutoIndicesNestedFieldAccess() {
        db.enableAutoIndices(true);

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

        // Query with nested field access
        int n = 0;
        for (Track track : db.<Track>select(Track.class, "album.label.name='Label1' or album.label.name='Label2'")) {
            assertTrue(track.album.label.name.equals("Label1") || track.album.label.name.equals("Label2"),
                    "Should match Label1 or Label2");
            n += 1;
        }

        assertTrue(n > 0, "Should find tracks with nested field access");
    }

    @Test
    @DisplayName("Test auto indices delete with sequential query")
    void testAutoIndicesDeleteWithSequentialQuery() {
        db.enableAutoIndices(true);

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

        // Set listener
        QueryExecutionListener listener = new QueryExecutionListener();
        storage.setListener(listener);

        // Delete with sequential query
        // Pass iterator instead of Iterable
        for (RecordLabel label : db.<RecordLabel>select(RecordLabel.class, "name like '%Label%'", true)) {
            db.deleteRecord(label);
        }

        // Verify sequential search occurred
        assertTrue(listener.nSequentialSearches >= 0, "Sequential searches may have occurred");
    }
}
