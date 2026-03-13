package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.example.zephyrevents.model.Event;

import org.junit.Before;
import org.junit.Test;

// Anthropic, Claude, "Write Mockito unit tests for EventRepository", 2026-03-12

public class EventRepositoryTest {

    private EventRepository repository;
    private RepositoryCallback<Void> mockVoidCallback;
    private RepositoryCallback<Event> mockEventCallback;

    @Before
    public void setup() {
        mockVoidCallback = mock(RepositoryCallback.class);
        mockEventCallback = mock(RepositoryCallback.class);
        // uses the injected constructor so no real Firebase connection
        repository = new EventRepository(mock(com.google.firebase.firestore.FirebaseFirestore.class));
    }

    @Test
    public void saveEvent_nullEvent_callsOnFailure() {
        repository.saveEvent(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveEvent_nullEvent_neverCallsOnSuccess() {
        repository.saveEvent(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void saveEvent_emptyEventId_callsOnFailure() {
        Event event = new Event();
        event.setEventId("");
        repository.saveEvent(event, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveEvent_nullEventId_callsOnFailure() {
        Event event = new Event();
        event.setEventId(null);
        repository.saveEvent(event, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveEvent_whitespaceEventId_callsOnFailure() {
        Event event = new Event();
        event.setEventId("   ");
        repository.saveEvent(event, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getEventById_nullId_callsOnFailure() {
        repository.getEventById(null, mockEventCallback);
        verify(mockEventCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getEventById_emptyId_callsOnFailure() {
        repository.getEventById("", mockEventCallback);
        verify(mockEventCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getEventById_whitespaceId_callsOnFailure() {
        repository.getEventById("   ", mockEventCallback);
        verify(mockEventCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getEventById_nullId_neverCallsOnSuccess() {
        repository.getEventById(null, mockEventCallback);
        verify(mockEventCallback, never()).onSuccess(any());
    }
}