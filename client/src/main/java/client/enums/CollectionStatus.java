package client.enums;

import javafx.scene.paint.Color;

public enum CollectionStatus {
    // These statuses are shown when the collection configuration is
    // not up-to-date and requires creation, addition, or update
    EXISTS_NOT_UP_TO_DATE("Item.collectionLocalAndServer",
            Color.GREEN, true, false, true),
    EXISTS_REMOTELY("Item.collectionInServer",
            Color.GREEN, true, false, false),
    DOES_NOT_EXIST("Item.collectionNotInServer",
            Color.GREY, true, false, false),

    // This status indicates that the collection configuration is
    // up-to-date and no further action is required
    EXISTS_UP_TO_DATE("Item.collectionUpToDate",
            Color.GREEN, true, true, true),

    // These statuses are displayed immediately after a user clicks a button
    CREATED("Item.collectionCreated",
            Color.GREEN, true, true, true),
    ADDED("Item.collectionAdded",
            Color.GREEN, true, true, true),
    UPDATED("Item.collectionUpdated",
            Color.GREEN, true, true, true),

    // These statuses indicate an error or issue
    EMPTY("Item.collectionEmpty",
            Color.RED, false, false, false),
    TITLE_NOT_UNIQUE("Item.collectionTitleNotUnique",
            Color.RED, false, false, false),
    SERVER_NOT_FOUND("Item.collectionServerNotFound",
            Color.RED, false, false, false),
    ERROR_DOES_NOT_EXIST("Item.collectionDeletedAndDoesNotExistInServer",
            Color.RED, false, false, false),
    ERROR("Item.collectionError",
            Color.RED, false, false, false);

    private final String languageKey;
    private final Color color;

    // Indicates if a collection can be saved with current status
    private final boolean success;

    // Indicates if a collection is fully up to date
    private final boolean upToDate;

    // Indicates if a collection can be the default one
    private final boolean canBeDefault;

    CollectionStatus(
            String languageKey,
            Color color,
            boolean canSave,
            boolean upToDate,
            boolean canBeDefault) {
        this.languageKey = languageKey;
        this.color = color;
        this.success = canSave;
        this.upToDate = upToDate;
        this.canBeDefault = canBeDefault;
    }

    public String getKey(){
        return languageKey;
    }

    public Color getColor() {
        return color;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isUpToDate() {
        return upToDate;
    }

    public boolean canBeDefault() {
        return canBeDefault;
    }
}
