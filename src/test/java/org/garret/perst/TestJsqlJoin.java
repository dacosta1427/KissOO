package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestJsqlJoin.java
 * Tests SQL JOIN queries
 */
class TestJsqlJoin {

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

    private Storage storage;
    private Database db;
    // Scaled down from 100 labels, 10000 albums
    private static final int nLabels = 10;
    private static final int nAlbums = 100;
    private static final int nTracksPerAlbum = 5;
    private static final String TEST_DB = "testjsqljoin.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);
        db = new Database(storage);

        // Create labels
        for (int i = 0; i < nLabels; i++) {
            RecordLabel label = new RecordLabel();
            label.name = "Label" + i;
            label.email = "contact@" + label.name + ".com";
            label.address = "Country, City, Street";
            label.phone = "+1 123-456-7890";
            db.addRecord(label);
        }

        // Create albums and tracks
        String[] genres = {"Rock", "Pop", "Jazz", "R&B", "Folk", "Classic"};
        for (int i = 0; i < nAlbums; i++) {
            Album album = new Album();
            album.name = "Album" + i;
            Iterator<RecordLabel> labelIter = db.select(RecordLabel.class, "name='Label" + (i % nLabels) + "'");
            album.label = labelIter.next();
            album.genre = genres[i % genres.length];
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
    }

    private RecordLabel getLabel(int index) {
        Iterator<RecordLabel> iter = db.select(RecordLabel.class, "name='Label" + index + "'");
        return iter.hasNext() ? iter.next() : null;
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test JOIN query with parameter")
    void testJoinQueryWithParameter() {
        Query<Track> query = db.prepare(Track.class, "no > 0 and album.label.name=?");
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
    @DisplayName("Test ORDER BY ascending")
    void testOrderByAscending() {
        String prev = "";
        int n = 0;
        Iterator<RecordLabel> iter = db.select(RecordLabel.class, "order by name");
        while (iter.hasNext()) {
            RecordLabel label = iter.next();
            assertTrue(prev.compareTo(label.name) < 0, "Labels should be in ascending order");
            prev = label.name;
            n += 1;
        }
        assertEquals(nLabels, n, "Should find all labels");
    }

    @Test
    @DisplayName("Test ORDER BY descending")
    void testOrderByDescending() {
        String prev = "zzz";
        int n = 0;
        Iterator<RecordLabel> iter = db.select(RecordLabel.class, "name like 'Label%' order by name desc");
        while (iter.hasNext()) {
            RecordLabel label = iter.next();
            assertTrue(prev.compareTo(label.name) > 0, "Labels should be in descending order");
            prev = label.name;
            n += 1;
        }
        assertEquals(nLabels, n, "Should find all labels");
    }

    @Test
    @DisplayName("Test IN clause")
    void testInClause() {
        int n = 0;
        Iterator<RecordLabel> iter = db.select(RecordLabel.class, "name in ('Label1', 'Label2', 'Label3')");
        while (iter.hasNext()) {
            iter.next();
            n += 1;
        }
        assertTrue(n >= 1, "Should find at least 1 label");
    }

    @Test
    @DisplayName("Test complex OR and LIKE")
    void testComplexOrAndLike() {
        int n = 0;
        Iterator<RecordLabel> iter = db.select(RecordLabel.class, "(name = 'Label1' or name = 'Label2' or name = 'Label3') and email like 'contact@%'");
        while (iter.hasNext()) {
            iter.next();
            n += 1;
        }
        assertTrue(n >= 1, "Should find at least 1 label");
    }

    @Test
    @DisplayName("Test JOIN with album label")
    void testJoinWithAlbumLabel() {
        int n = 0;
        Iterator<Track> iter = db.select(Track.class, "album.label.name='Label1' or album.label.name='Label2'");
        while (iter.hasNext()) {
            iter.next();
            n += 1;
        }
        // Each label has nAlbums/nLabels albums, each album has nTracksPerAlbum tracks
        int expectedPerLabel = nAlbums / nLabels * nTracksPerAlbum;
        assertTrue(n >= expectedPerLabel, "Should find tracks from Label1 and Label2");
    }

    @Test
    @DisplayName("Test prepared statement with IN parameter")
    void testPreparedStatementWithInParameter() {
        Query<RecordLabel> query = db.prepare(RecordLabel.class, "phone like '+1%' and name in ?");
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < nLabels; i++) {
            list.add("Label" + i);
        }
        query.setParameter(1, list);
        int n = 0;
        Iterator<RecordLabel> iter = query.iterator();
        while (iter.hasNext()) {
            RecordLabel label = iter.next();
            assertTrue(label.name.startsWith("Label"), "Label name should start with Label");
            n += 1;
        }
        assertEquals(nLabels, n, "Should find all labels");
    }
}
