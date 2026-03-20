package commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NoteTest {

    @Test
    void testConstructorAndGetters() {
        String title = "Test Title";
        String content = "This is a test note.";
        Collection collection = new Collection("This is a test collection");

        Note note = new Note(title, collection, content);

        assertEquals(title, note.title);
        assertEquals(content, note.content);
    }

    @Test
    void testNoArgsConstructor() {
        Note note = new Note();

        assertNull(note.title);
        assertNull(note.content );
    }

    @Test
    void testEquals_SameObject() {
        Collection collection = new Collection("This is a test collection");
        Note note = new Note("Test Title", collection, "This is a test note.");
        assertEquals(note, note);
    }

    @Test
    void testEquals_NullObject() {
        Collection collection = new Collection("This is a test collection");
        Note note = new Note("Test Title", collection, "This is a test note.");
        assertNotEquals(note, null);
    }

    @Test
    void testEquals_DifferentClass() {
        Collection collection = new Collection("This is a test collection");
        Note note = new Note("Test Title", collection, "This is a test note.");
        String other = "Some String";
        assertNotEquals(note, other);
    }

    @Test
    void testEquals_DifferentValues() {
        Collection collection = new Collection("This is a test collection");
        Note note1 = new Note("Title 1", collection, "Content 1");
        Note note2 = new Note("Title 2", collection, "Content 2");

        assertNotEquals(note1, note2);
    }

    @Test
    void testEquals_DifferentCollections() {
        Collection collection1 = new Collection("This is a test collection 1");
        Collection collection2 = new Collection("This is a test collection 2");
        Note note1 = new Note("Title", collection1, "Content");
        Note note2 = new Note("Title", collection2, "Content");

        assertNotEquals(note1, note2);
    }

    @Test
    void testEquals_SameValues() {
        Collection collection1 = new Collection("This is a test collection");
        Collection collection2 = new Collection("This is a test collection");
        String title = "Test Title";
        String content = "This is a test note.";

        Note note1 = new Note(title, collection1, content);
        Note note2 = new Note(title, collection2, content);

        assertEquals(note1, note2);
    }

    @Test
    void testHashCode_SameValues() {
        Collection collection = new Collection("This is a test collection");
        String title = "Test Title";
        String content = "This is a test note.";

        Note note1 = new Note(title, collection, content);
        Note note2 = new Note(title, collection, content);

        assertEquals(note1.hashCode(), note2.hashCode());
    }

    @Test
    void testHashCode_DifferentValues() {
        Collection collection = new Collection("This is a test collection");
        Note note1 = new Note("Title 1", collection, "Content 1");
        Note note2 = new Note("Title 2", collection, "Content 2");

        assertNotEquals(note1.hashCode(), note2.hashCode());
    }

    @Test
    void testToString() {
        String title = "Test Title";
        String content = "This is a test note.";
        Collection collection = new Collection("This is a test collection");

        Note note = new Note(title, collection, content);

        String toStringValue = note.toString();
        assertTrue(toStringValue.contains("title=Test Title"));
        assertTrue(toStringValue.contains("content=This is a test note."));
    }
}
