package service;

import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.database.CollectionRepository;
import server.database.NotesRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    CollectionService service;

    @Mock
    CollectionRepository repo;
    @Mock
    NotesRepository notesRepo;

    @BeforeEach
    void setup() {
        service = new CollectionService(repo, notesRepo);
    }

    @Test
    void createCollectionTest_Success() {
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        when(repo.save(collection)).thenReturn(collection);
        when(repo.findById(collectionKey)).thenReturn(Optional.empty());

        var result = service.createCollection(collectionKey);

        verify(repo, times(1)).save(collection);
        verify(repo, times(1)).findById(collectionKey);
        assertEquals(Optional.of(collection), result);
    }

    @Test
    void createCollectionTest_AlreadyExists() {
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        when(repo.findById(collectionKey)).thenReturn(Optional.of(collection));

        var result = service.createCollection(collectionKey);

        verify(repo, times(0)).save(collection);
        verify(repo, times(1)).findById(collectionKey);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void getCollectionTest_Success() {
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        when(repo.findById(collectionKey)).thenReturn(Optional.of(collection));

        var result = service.getCollection(collectionKey);

        verify(repo, times(1)).findById(collectionKey);
        assertEquals(Optional.of(collection), result);
    }

    @Test
    void getCollectionTest_NoKey() {
        var result = service.getCollection(null);

        verify(repo, times(0)).findById(null);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void deleteCollectionByKey_Success() {
        String collectionKey = "collection key";
        Collection collection = new Collection(collectionKey);
        when(repo.findById(collectionKey)).thenReturn(Optional.of(collection));

        var result = service.deleteCollectionByKey(collectionKey);

        assertTrue(result);
        verify(repo, times(1)).findById(collectionKey);
        verify(repo, times(1)).delete(collection);
    }

    @Test
    void deleteCollectionByKey_NotFound() {
        String collectionKey = "collection key";
        when(repo.findById(collectionKey)).thenReturn(Optional.empty());

        var result = service.deleteCollectionByKey(collectionKey);

        assertFalse(result);
        verify(repo, times(1)).findById(collectionKey);
        verify(repo, times(0)).deleteById(collectionKey);
    }
}