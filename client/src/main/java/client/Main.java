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
package client;

import client.exceptions.ExceptionHandler;
import client.scenes.CreateNoteCtrl;
import client.scenes.EditCollectionsCtrl;
import client.scenes.MainCtrl;
import client.scenes.NotesHomeCtrl;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {
    private static Injector injector;

    private static MyFXML fxml;

    public static String configFileId = null;

    public static final String BUNDLE_NAME = "Items";
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static void main(String[] args) {
        if (args.length > 0 && !args[0].isEmpty()) {
            configFileId = args[0];
        }

        injector = createInjector(new MyModule(configFileId));
        fxml = new MyFXML(injector);
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        var bundle = ResourceBundle.getBundle(BUNDLE_NAME, DEFAULT_LOCALE);

        var notesHome = fxml.load(NotesHomeCtrl.class, bundle, "client", "scenes",
                "NotesHome.fxml");
        var editCollections = fxml.load(EditCollectionsCtrl.class, bundle, "client", "scenes",
                "EditCollections.fxml");
        var createNote = fxml.load(CreateNoteCtrl.class, bundle, "client", "scenes",
                "CreateNote.fxml");
        var mainCtrl = injector.getInstance(MainCtrl.class);
        var exceptionHandler = injector.getInstance(ExceptionHandler.class);

        Thread.setDefaultUncaughtExceptionHandler(mainCtrl::showException);

        mainCtrl.initialize(primaryStage, notesHome, exceptionHandler, editCollections, createNote);

        mainCtrl.addClickListener();
    }

}