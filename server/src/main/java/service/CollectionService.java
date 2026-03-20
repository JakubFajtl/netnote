package service;

import commons.Collection;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;
import server.database.NotesRepository;

import java.util.Optional;

@Service
public class CollectionService {

    private final CollectionRepository repo;
    private final NotesRepository notesRepo;
    // Constructor injection
    public CollectionService(CollectionRepository repo, NotesRepository notesRepo) {
        this.repo = repo;
        this.notesRepo = notesRepo;
    }

    /**
     * Creates a collection with a given collection key
     * @param collectionKey the key of a collection to create
     * @return the created collection (if successful)
     */
    public Optional<Collection> createCollection(String collectionKey) {
        if (getCollection(collectionKey).isPresent()) {
            return Optional.empty();
        }
        return Optional.of(repo.save(new Collection(collectionKey)));
    }

    /**
     * Gets a collection with a given collection key
     * Basically the method just checks if a collection exists
     * @param collectionKey the key of a collection to get
     * @return the collection from the database (if it exists)
     */
    public Optional<Collection> getCollection(String collectionKey) {
        if (collectionKey == null) {
            return Optional.empty();
        }
        return repo.findById(collectionKey);
    }

    /**
     * Deletes a collection from the database
     * @param collectionKey the key of a collection to delete
     * @return the boolean indicating if a collection was deleted
     */
    @Transactional
    public boolean deleteCollectionByKey(String collectionKey) {
        Optional<Collection> collectionToDelete = getCollection(collectionKey);
        if (collectionToDelete.isEmpty()) {
            return false; // collection does not exist
        }
        Collection collection = collectionToDelete.get();

        // Delete all notes in the collection
        notesRepo.deleteByCollection(collection);

        repo.delete(collection);

        return true;
    }
}