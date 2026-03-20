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

import client.exceptions.*;
import client.scenes.EditCollectionsCtrl;
import client.scenes.NotesHomeCtrl;
import client.services.*;
import client.utils.*;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import client.scenes.MainCtrl;

public class MyModule implements Module {
    private final String configId;

    public MyModule(String configId) {
        this.configId = configId;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(NotesHomeCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditCollectionsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(HealthCheckClient.class).to(ServerHealthCheckClient.class).in(Scopes.SINGLETON);
        binder.bind(ConfigService.class).toInstance(
                new ConfigService(configId, new LanguageService()));
        binder.bind(NotesClient.class).to(NotesApiClient.class).in(Scopes.SINGLETON);
        binder.bind(CollectionClient.class).to(CollectionApiClient.class).in(Scopes.SINGLETON);
        binder.bind(FilesClient.class).to(FilesApiClient.class).in(Scopes.SINGLETON);
        binder.bind(ExceptionHandler.class).to(AlertExceptionHandler.class).in(Scopes.SINGLETON);
        binder.bind(ResponseValidator.class).to(ApiResponseValidator.class).in(Scopes.SINGLETON);
        binder.bind(SelectCollectionsService.class).in(Scopes.SINGLETON);
        binder.bind(NotesService.class).in(Scopes.SINGLETON);
        binder.bind(ServerValidator.class).in(Scopes.SINGLETON);
        binder.bind(NotesMigrationService.class).in(Scopes.SINGLETON);
    }
}