package server.api;

import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import server.ErrorResponseBuilder;
import service.CollectionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    CollectionController controller;

    @Mock
    CollectionService service;

    @Mock
    ErrorResponseBuilder errorResponseBuilder;

    @BeforeEach
    void setup() {
        controller = new CollectionController(service, errorResponseBuilder);
    }


    @Test
    void createCollectionTest_Success() {
        String key = "collection key";
        Collection expectedCollection = new Collection(key);
        when(service.createCollection(key)).thenReturn(Optional.of(expectedCollection));

        var response = controller.createCollection(key);

        assertEquals(expectedCollection, response.getBody());
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(service, times(1)).createCollection(key);
    }

    @Test
    void createCollectionTest_BadRequest() {
        String key = "collection key";
        var expectedResponse = ResponseEntity.badRequest().build();
        when(service.createCollection(key)).thenReturn(Optional.empty());
        when(errorResponseBuilder.buildBadRequest(any())).thenReturn(expectedResponse);

        var response = controller.createCollection(key);

        assertEquals(expectedResponse, response);
        verify(service, times(1)).createCollection(key);
        verify(errorResponseBuilder, times(1)).buildBadRequest(any());
    }

    @Test
    void createCollectionTest_ServerError() {
        String key = "collection key";
        Exception exception = new RuntimeException();
        var expectedResponse = ResponseEntity.internalServerError().build();
        when(service.createCollection(key)).thenThrow(exception);
        when(errorResponseBuilder.buildServerError(any())).thenReturn(expectedResponse);

        var response = controller.createCollection(key);

        assertEquals(expectedResponse, response);
        verify(service, times(1)).createCollection(key);
        verify(errorResponseBuilder, times(1)).buildServerError(exception);
    }

    @Test
    void checkIfCollectionExistsTest_Success() {
        String key = "collection key";
        Collection expectedCollection = new Collection(key);
        when(service.getCollection(key)).thenReturn(Optional.of(expectedCollection));

        var response = controller.checkIfCollectionExists(key);

        assertEquals(true, response.getBody());
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(service, times(1)).getCollection(key);
    }

    @Test
    void checkIfCollectionExistsTest_ServerError() {
        String key = "collection key";
        Exception exception = new RuntimeException();
        var expectedResponse = ResponseEntity.internalServerError().build();
        when(service.getCollection(key)).thenThrow(exception);
        when(errorResponseBuilder.buildServerError(any())).thenReturn(expectedResponse);

        var response = controller.checkIfCollectionExists(key);

        assertEquals(expectedResponse, response);
        verify(service, times(1)).getCollection(key);
        verify(errorResponseBuilder, times(1)).buildServerError(exception);
    }

    @Test
    void deleteCollectionTest_Success() {
        String key = "collection key";
        when(service.deleteCollectionByKey(key)).thenReturn(true);

        var response = controller.deleteCollection(key);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        verify(service, times(1)).deleteCollectionByKey(key);
    }

    @Test
    void deleteCollectionTest_NotFound() {
        String key = "collection key";
        var expectedResponse = ResponseEntity.notFound().build();
        when(service.deleteCollectionByKey(key)).thenReturn(false);
        when(errorResponseBuilder.buildNotFound(any())).thenReturn(expectedResponse);

        var response = controller.deleteCollection(key);

        assertEquals(expectedResponse, response);
        verify(service, times(1)).deleteCollectionByKey(key);
        verify(errorResponseBuilder, times(1)).buildNotFound(any());
    }

    @Test
    void deleteCollectionTest_ServerError() {
        String key = "collection key";
        Exception exception = new RuntimeException();
        var expectedResponse = ResponseEntity.internalServerError().build();
        when(service.deleteCollectionByKey(key)).thenThrow(exception);
        when(errorResponseBuilder.buildServerError(any())).thenReturn(expectedResponse);

        var response = controller.deleteCollection(key);

        assertEquals(expectedResponse, response);
        verify(service, times(1)).deleteCollectionByKey(key);
        verify(errorResponseBuilder, times(1)).buildServerError(exception);
    }
}