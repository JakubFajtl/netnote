package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteIdentificationTest {

    @Test
    void testConstructorAndGetters() {
        String title = "Test Title";
        Collection collection = new Collection("Test collection");

        NoteIdentification noteIdentification = new NoteIdentification(title, collection);

        assertEquals(title, noteIdentification.title);
    }

    @Test
    void testCopyConstructor() {
        Note note = new Note();
        note.setId(123);
        note.title = "Copied Title";

        NoteIdentification noteIdentification = new NoteIdentification(note);

        assertEquals(123, noteIdentification.id);
        assertEquals("Copied Title", noteIdentification.title);
    }

    @Test
    void testNoArgsConstructor() {
        NoteIdentification noteIdentification = new NoteIdentification();

        assertEquals(0, noteIdentification.id);
        assertNull(noteIdentification.title);
    }

    @Test
    void testEquals_SameObject() {
        Collection collection = new Collection("Test collection");
        NoteIdentification noteIdentification = new NoteIdentification("Test Title", collection);

        assertEquals(noteIdentification, noteIdentification);
    }

    @Test
    void testEquals_NullObject() {
        Collection collection = new Collection("Test collection");
        NoteIdentification noteIdentification = new NoteIdentification("Test Title", collection);

        assertNotEquals(noteIdentification, null);
    }

    @Test
    void testEquals_DifferentClass() {
        Collection collection = new Collection("Test collection");
        NoteIdentification noteIdentification = new NoteIdentification("Test Title", collection);

        String other = "Some String";
        assertNotEquals(noteIdentification, other);
    }

    @Test
    void testEquals_DifferentValues() {
        Collection collection1 = new Collection("Test collection");
        NoteIdentification note1 = new NoteIdentification("Title 1", collection1);
        note1.setId(1);

        Collection collection2 = new Collection("Test collection");
        NoteIdentification note2 = new NoteIdentification("Title 2", collection2);
        note2.setId(2);

        assertNotEquals(note1, note2);
    }

    @Test
    void testEquals_SameValues() {
        Collection collection1 = new Collection("Test collection");
        NoteIdentification note1 = new NoteIdentification("Test Title", collection1);
        note1.setId(123);

        Collection collection2 = new Collection("Test collection");
        NoteIdentification note2 = new NoteIdentification("Test Title", collection2);
        note2.setId(123);

        assertEquals(note1, note2);
    }

    @Test
    void testHashCode_SameValues() {
        Collection collection1 = new Collection("Test collection");
        NoteIdentification note1 = new NoteIdentification("Test Title", collection1);
        note1.setId(123);

        Collection collection2 = new Collection("Test collection");
        NoteIdentification note2 = new NoteIdentification("Test Title", collection2);
        note2.setId(123);

        assertEquals(note1.hashCode(), note2.hashCode());
    }

    @Test
    void testHashCode_DifferentValues() {
        Collection collection1 = new Collection("Test collection 1");
        NoteIdentification note1 = new NoteIdentification("Title 1", collection1);
        note1.setId(1);

        Collection collection2 = new Collection("Test collection 2");
        NoteIdentification note2 = new NoteIdentification("Title 2", collection2);
        note2.setId(1);

        assertNotEquals(note1.hashCode(), note2.hashCode());
    }

    @Test
    void testToString() {
        Collection collection = new Collection("Test collection");
        NoteIdentification noteIdentification = new NoteIdentification("Test Title", collection);
        noteIdentification.setId(123);

        String toStringValue = noteIdentification.toString();
        assertTrue(toStringValue.contains("title=Test Title"));
        assertTrue(toStringValue.contains("id=123"));
    }

    @Test
    void testSetId() {
        NoteIdentification noteIdentification = new NoteIdentification();
        noteIdentification.setId(123);

        assertEquals(123, noteIdentification.id);
    }
}
