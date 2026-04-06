package com.example.zephyrevents.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;

// Gemini 3.1 Pro Preview, Google AiStudio, "Could you rewrite these tests for the updated EventController using junit and mockito." - 2023-04-05

public class EventControllerTest {

    private EventRepository mockRepo;
    private EventController eventController;

    @Before
    public void setUp() throws Exception {
        mockRepo = Mockito.mock(EventRepository.class);

        // To use mock Firebase
        eventController = newEventControllerWithoutConstructor();

        // Inject mock repository into the controller via reflection
        Field repoField = EventController.class.getDeclaredField("eventRepository");
        repoField.setAccessible(true);
        repoField.set(eventController, mockRepo);

        // Also set the singleton instance field to our test instance (defensive)
        Field instanceField = EventController.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, eventController);
    }

    private static EventController newEventControllerWithoutConstructor() throws Exception {
        // Use Unsafe to allocate without running the private constructor.
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);
        return (EventController) unsafeClass
                .getMethod("allocateInstance", Class.class)
                .invoke(unsafe, EventController.class);
    }

    @Test
    public void createEvent_callsRepo() {
        Event event = new Event();
        RepositoryCallback<Void> callback = Mockito.mock(RepositoryCallback.class);

        eventController.createEvent(event, callback);

        verify(mockRepo).saveEvent(eq(event), eq(callback));
    }

    @Test
    public void deleteEvent_callsRepo() {
        String eventId = "event123";
        RepositoryCallback<Void> callback = Mockito.mock(RepositoryCallback.class);

        eventController.deleteEvent(eventId, callback);

        verify(mockRepo).deleteEvent(eq(eventId), eq(callback));
    }

    @Test
    public void getAllEvents_callsRepo() {
        RepositoryCallback<List<Event>> callback = Mockito.mock(RepositoryCallback.class);

        eventController.getAllEvents(callback);

        verify(mockRepo).getAllEvents(eq(callback));
    }

    @Test
    public void getEventById_callsRepo() {
        String eventId = "event123";
        RepositoryCallback<Event> callback = Mockito.mock(RepositoryCallback.class);

        eventController.getEventById(eventId, callback);

        verify(mockRepo).getEventById(eq(eventId), eq(callback));
    }
}