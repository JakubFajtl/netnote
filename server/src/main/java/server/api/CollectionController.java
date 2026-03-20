package server.api;

import commons.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.ErrorResponseBuilder;
import service.CollectionService;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final ErrorResponseBuilder errorResponseBuilder;

    // Constructor using CollectionService and errorResponseBuilder
    public CollectionController(
            CollectionService collectionService,
            ErrorResponseBuilder errorResponseBuilder) {
        this.collectionService = collectionService;
        this.errorResponseBuilder = errorResponseBuilder;
    }

    /**
     * REST API endpoint for creating collections
     * @return Response entity that (if successful) contains the key of created collection
     */
    @PostMapping("/{collectionKey}")
    public ResponseEntity<Collection> createCollection(
            @PathVariable("collectionKey") String collectionKey) {
        try {
            var savedCollection = collectionService.createCollection(collectionKey);
            return savedCollection.map(ResponseEntity::ok)
                    .orElseGet(() -> errorResponseBuilder.buildBadRequest(
                            "Item.collectionAlreadyInServer"));
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    /**
     * REST API endpoint for checking if a collection exists
     * @return A response entity with a boolean that is true iff a collection exists in the server
     */
    @GetMapping("/{collectionKey}/exists")
    public ResponseEntity<Boolean> checkIfCollectionExists(
            @PathVariable("collectionKey") String collectionKey) {
        try {
            return ResponseEntity.ok(
                    collectionService.getCollection(collectionKey).isPresent()
            );
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }

    /**
     * REST API endpoint for deleting collections
     * @return A response entity without a body
     */
    @DeleteMapping("/{collectionKey}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable("collectionKey") String collectionKey) {
        try {
            var wasDeleted = collectionService.deleteCollectionByKey(collectionKey);
            if (!wasDeleted) {
                return errorResponseBuilder.buildNotFound(
                        "Item.deletedCollectionNotInServer");
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return errorResponseBuilder.buildServerError(e);
        }
    }
}