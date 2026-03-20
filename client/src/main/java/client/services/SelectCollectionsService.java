package client.services;

import client.utils.NotesClient;
import commons.CollectionConfig;
import commons.NoteIdentification;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This service manages the state of selected collections
 */
public class SelectCollectionsService {

    private final ConfigService configService;
    private final NotesClient notesClient;
    private final EditCollectionsService collectionsService;
    private final NotesService notesService;

    private List<String> selectedCollectionKeys;

    @Inject
    public SelectCollectionsService(
            ConfigService configService,
            NotesClient notesClient,
            NotesService notesService,
            EditCollectionsService collectionsService) {
        this.configService = configService;
        this.notesClient = notesClient;
        this.notesService = notesService;
        this.collectionsService = collectionsService;

        selectedCollectionKeys = new ArrayList<>();
    }

    public void resetSelection() {
        selectedCollectionKeys = new ArrayList<>();
        selectCollection(collectionsService.getIndexOfDefaultConfigKey());
    }

    public boolean areAllSelected() {
        return selectedCollectionKeys.size() ==
                configService.getAllCollectionConfigs().size();
    }

    /**
     * Selects the collection with a given index
     * @param collectionIndex the index to select
     */
    public void selectCollection(int collectionIndex) {
        CollectionConfig config = configService.getCollectionConfig(collectionIndex);

        if (!selectedCollectionKeys.contains(config.collectionKey)) {
            selectedCollectionKeys.add(config.collectionKey);
        }
    }

    /**
     * Unselects the collection with a given index
     * @param collectionIndex the index to unselect
     */
    public void unselectCollection(int collectionIndex) {
        CollectionConfig config = configService.getCollectionConfig(collectionIndex);

        selectedCollectionKeys.remove(config.collectionKey);
    }

    public void pullAllNotesInSelectedCollections() {
        pullNotes(notesClient::getAllNoteInfos);
    }

    public void pullFilteredNoteInfosInSelectedCollections(String query) {
        pullNotes(key -> notesClient.getFilteredNoteInfos(query, key));
    }

    /**
     * Pulls the notes with a given function and
     * sets the data of the sidebar in notesService
     * @param getNotesInCollection the function that describes
     *                             how to get notes in a specific collection
     */
    private void pullNotes(Function<CollectionConfig,
            List<NoteIdentification>> getNotesInCollection) {
        List<NoteIdentification> notes = new ArrayList<>();

        for (String collectionKey : selectedCollectionKeys) {
            notes.addAll(getNotesInCollection
                    .apply(configService.getCollectionConfig(collectionKey)));
        }

        notesService.setSidebarData(notes);
    }

    /**
     * Deletes all collections in the list that do not exist in the config
     */
    public void keepOnlyValidSelectedCollections() {
        selectedCollectionKeys = selectedCollectionKeys
                .stream()
                .filter(key -> configService.getIndexOfConfigKey(key).isPresent())
                .collect(Collectors.toCollection(ArrayList::new));

        if (selectedCollectionKeys.isEmpty()) {
            resetSelection();
        }
    }

    /**
     * Gets the indexes of all selected collections
     * @return the indexes of selected collections
     */
    public List<Integer> getSelectedCollectionsIndexes() {
        return selectedCollectionKeys
                .stream()
                .map(configService::getIndexOfConfigKey)
                .filter(OptionalInt::isPresent)
                .map(OptionalInt::getAsInt)
                .toList();
    }
}
