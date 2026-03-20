/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.exceptions.*;
import commons.constants.AppConstants;
import jakarta.inject.Inject;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import client.services.LanguageService;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

public class MainCtrl {

    private Stage primaryStage;
    private Stage collectionsStage;
    private Stage createNoteStage;

    private NotesHomeCtrl notesHomeCtrl;
    private Scene notesHome;
    private ExceptionHandler exceptionHandler;
    private final LanguageService languageService;

    private CreateNoteCtrl createNoteCtrl;
    private Scene createNote;

    private EditCollectionsCtrl editCollectionsCtrl;
    private Scene editCollections;

    private TitleListCellCtrl activeCellCtrl = null;

    private final Map<Integer, TitleListCellCtrl> cellControllers = new HashMap<>();

    @Inject
    public MainCtrl(
            LanguageService languageService) {
        this.languageService = languageService;
    }


    public void initialize(
            Stage primaryStage,
            Pair<NotesHomeCtrl, Parent> notesHome,
            ExceptionHandler exceptionHandler,
            Pair<EditCollectionsCtrl, Parent> editCollections,
            Pair<CreateNoteCtrl, Parent> createNote) {

        this.primaryStage = primaryStage;
        this.notesHomeCtrl = notesHome.getKey();
        this.notesHome = new Scene(notesHome.getValue());
        this.exceptionHandler = exceptionHandler;
        this.editCollectionsCtrl = editCollections.getKey();
        this.editCollections = new Scene(editCollections.getValue());
        this.createNoteCtrl = createNote.getKey();
        this.createNote = new Scene(createNote.getValue());

        showNotesHome();
        primaryStage.show();

        addClickListener();
    }

    public NotesHomeCtrl getNotesHomeCtrl() {
        return notesHomeCtrl;
    }

    /**
     * The method loads the main page of the notes application
     */
    public void showNotesHome() {
        primaryStage.setTitle(AppConstants.AppName);
        primaryStage.setScene(notesHome);
        notesHomeCtrl.refreshApp();
    }

    public void showException(Thread thread, Throwable throwable) {
        exceptionHandler.showException(
                thread,
                throwable,
                ((buttonType, exception) -> {
                    if (buttonType == ButtonType.OK) {
                        if (exception instanceof NotesApiException ||
                                exception instanceof ConfigException ||
                                exception.getCause() instanceof NotesApiException
                                || exception instanceof TitleNotUniqueException
                                || exception.getCause()
                                instanceof TitleNotUniqueException) {
                            editCollectionsCtrl.removeInvalidConfigs();
                            editCollectionsCtrl.updateListViewCollectionList();
                            editCollectionsCtrl.clearTextFields();
                            notesHomeCtrl.refreshApp();
                            notesHomeCtrl.checkThread();
                        }
                        else if (exception instanceof DeleteDefaultException) {
                            editCollectionsCtrl.removeInvalidConfigs();
                            editCollectionsCtrl.updateListViewCollectionList();
                            notesHomeCtrl.refreshApp();
                            notesHomeCtrl.checkThread();
                        }
                        else if (exception instanceof ConnectException ||
                                exception instanceof ServerException) {
                            if (collectionsStage == null ||
                                    !collectionsStage.isShowing()) {
                                showCollections();
                            }
                        }
                    }
                    else if (buttonType.getButtonData().isCancelButton()) {
                        System.exit(0);
                    }
                })
        );
    }
    /**
     * Opens a new window for editing collections
     */
    public void showCollections(){
        if (collectionsStage == null) {
            collectionsStage = new Stage();
            collectionsStage.initOwner(primaryStage);
            collectionsStage.initModality(Modality.WINDOW_MODAL); // This makes the new window
            collectionsStage.setTitle(languageService.getDescriptionByKey("Item.collections"));
            collectionsStage.setScene(editCollections);

            // When the collections scene is closed,
            // it is convenient to refresh notes to load new changes
            collectionsStage.setOnCloseRequest(_ -> notesHomeCtrl.refreshApp());
        }
        collectionsStage.setResizable(false); //:P
        editCollectionsCtrl.updateListViewCollectionList();
        collectionsStage.showAndWait(); // This shows the new stage and waits for it to be closed
    }

    public void addClickListener() {
        notesHome.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (activeCellCtrl != null) {
                if (activeCellCtrl.isEditing() &&
                        activeCellCtrl.isClickInsideSection(event,
                                activeCellCtrl.getTitleTextField())
                        || activeCellCtrl.isClickInsideSection(event,
                        activeCellCtrl.getSaveButton())) {
                    return; // it should ignore inside the save button and the text field
                }

                activeCellCtrl.switchToReadMode();
                activeCellCtrl = null;
            }
        });
    }

    public TitleListCellCtrl getActiveCellCtrl() {
        return activeCellCtrl;
    }

    public void setActiveCellCtrl(TitleListCellCtrl activeCellCtrl) {
        this.activeCellCtrl = activeCellCtrl;
    }

    public void registerCellController(int index, TitleListCellCtrl ctrl) {
        cellControllers.put(index, ctrl);
    }

    public TitleListCellCtrl getCellController(int index) {
        return cellControllers.get(index);
    }

    public void showCreateNotePopup() {
        if (createNoteStage == null) {
            createNoteStage = new Stage();
            createNoteStage.initOwner(primaryStage);
            createNoteStage.initModality(Modality.WINDOW_MODAL);
            createNoteStage.setTitle(languageService.getDescriptionByKey("Item.createNewNote"));
            createNoteStage.setScene(createNote);

            createNoteStage.setOnCloseRequest(_ -> {
                var note = notesHomeCtrl.getServiceCurrentNote();
                notesHomeCtrl.refreshApp();
                if (note != null) notesHomeCtrl.handleRefreshNotes(note);
            });
        }
        createNoteStage.setResizable(false);
        createNoteCtrl.setCollectionsAndFocusTitle();
        createNoteStage.showAndWait();
    }
}