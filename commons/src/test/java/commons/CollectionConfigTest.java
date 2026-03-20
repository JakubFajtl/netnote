package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionConfigTest {

    @Test
    void testConstructorAndGetters() {
        String collectionKey = "testKey";
        String title = "Test Title";
        String serverURL = "http://test.server.com";

        CollectionConfig config = new CollectionConfig(collectionKey, title, serverURL);

        assertEquals(collectionKey, config.collectionKey);
        assertEquals(title, config.title);
        assertEquals(serverURL, config.serverURL);
    }

    @Test
    void testEquals_SameObject() {
        CollectionConfig config = new CollectionConfig("testKey", "Test Title", "http://test.server.com");
        assertEquals(config, config);
    }

    @Test
    void testEquals_NullObject() {
        CollectionConfig config = new CollectionConfig("testKey", "Test Title", "http://test.server.com");
        assertNotEquals(config, null);
    }

    @Test
    void testEquals_DifferentClass() {
        CollectionConfig config = new CollectionConfig("testKey", "Test Title", "http://test.server.com");
        String other = "test string";
        assertNotEquals(config, other);
    }

    @Test
    void testEquals_DifferentValues() {
        CollectionConfig config1 = new CollectionConfig("key1", "Title1", "http://server1.com");
        CollectionConfig config2 = new CollectionConfig("key2", "Title2", "http://server2.com");
        assertNotEquals(config1, config2);
    }

    @Test
    void testEquals_SameValues() {
        String collectionKey = "testKey";
        String title = "Test Title";
        String serverURL = "http://test.server.com";

        CollectionConfig config1 = new CollectionConfig(collectionKey, title, serverURL);
        CollectionConfig config2 = new CollectionConfig(collectionKey, title, serverURL);

        assertEquals(config1, config2);
    }

    @Test
    void testHashCode_SameValues() {
        String collectionKey = "testKey";
        String title = "Test Title";
        String serverURL = "http://test.server.com";

        CollectionConfig config1 = new CollectionConfig(collectionKey, title, serverURL);
        CollectionConfig config2 = new CollectionConfig(collectionKey, title, serverURL);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testHashCode_DifferentValues() {
        CollectionConfig config1 = new CollectionConfig("key1", "Title1", "http://server1.com");
        CollectionConfig config2 = new CollectionConfig("key2", "Title2", "http://server2.com");

        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        String collectionKey = "testKey";
        String title = "Test Title";
        String serverURL = "http://test.server.com";

        CollectionConfig config = new CollectionConfig(collectionKey, title, serverURL);

        String toStringValue = config.toString();
        assertTrue(toStringValue.contains("collectionKey=testKey"));
        assertTrue(toStringValue.contains("title=Test Title"));
        assertTrue(toStringValue.contains("serverURL=http://test.server.com"));
    }
}