package commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectionTest {

    @Test
    void testConstructorAndGetter() {
        String key = "testKey";
        Collection collection = new Collection(key);
        assertEquals(key, collection.collectionKey);
    }

    @Test
    void testNoArgsConstructor() {
        Collection collection = new Collection();
        assertNull(collection.collectionKey);
    }

    @Test
    void testEquals_SameObject() {
        Collection collection = new Collection("testKey");
        assertEquals(collection, collection);
    }

    @Test
    void testEquals_NullObject() {
        Collection collection = new Collection("testKey");
        assertNotEquals(collection, null);
    }

    @Test
    void testEquals_DifferentClass() {
        Collection collection = new Collection("testKey");
        String other = "test string";
        assertNotEquals(collection, other);
    }

    @Test
    void testEquals_DifferentKeys() {
        Collection collection1 = new Collection("key1");
        Collection collection2 = new Collection("key2");
        assertNotEquals(collection1, collection2);
    }

    @Test
    void testEquals_SameKeys() {
        String key = "testKey";
        Collection collection1 = new Collection(key);
        Collection collection2 = new Collection(key);
        assertEquals(collection1, collection2);
    }

    @Test
    void testHashCode_SameKeys() {
        String key = "testKey";
        Collection collection1 = new Collection(key);
        Collection collection2 = new Collection(key);
        assertEquals(collection1.hashCode(), collection2.hashCode());
    }

    @Test
    void testHashCode_DifferentKeys() {
        Collection collection1 = new Collection("key1");
        Collection collection2 = new Collection("key2");
        assertNotEquals(collection1.hashCode(), collection2.hashCode());
    }

    @Test
    void testToString() {
        String key = "testKey";
        Collection collection = new Collection(key);
        String toStringValue = collection.toString();
        assertTrue(toStringValue.contains("collectionKey=testKey"));
    }
}