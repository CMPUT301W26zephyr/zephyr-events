package com.example.zephyrevents;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EventControllerTest {

    private EventRepository mockRepo;
    private EventController eventController;

    @Before
    public void setUp() throws Exception {
        mockRepo = Mockito.mock(EventRepository.class);

        // Avoid EventController.getInstance() here: its constructor creates a real EventRepository
        // which touches Firebase/Android APIs and crashes in local JVM unit tests.
        eventController = newEventControllerWithoutConstructor();

        // Inject mock repository into the controller via reflection
        Field repoField = EventController.class.getDeclaredField("eventRepository");
        repoField.setAccessible(true);
        repoField.set(eventController, mockRepo);

        // Initialize in-memory lists so each test starts clean
        Field lotteriesField = EventController.class.getDeclaredField("mockLotteries");
        lotteriesField.setAccessible(true);
        lotteriesField.set(eventController, new ArrayList<>());

        Field historyField = EventController.class.getDeclaredField("mockHistory");
        historyField.setAccessible(true);
        historyField.set(eventController, new ArrayList<>());

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
    // Verifies that when you ask the controller to create an event, it forwards the request to the repository
    public void createEvent_callsRepo() {
        Event event = new Event();
        RepositoryCallback<Void> callback = Mockito.mock(RepositoryCallback.class);

        eventController.createEvent(event, callback);

        verify(mockRepo).saveEvent(eq(event), eq(callback));
    }

    @Test
    // Verifies that when you ask the controller to create an event, it forwards the request to the repository
    // this checks the controller's waitlist logic works and stores the right data
    public void addToWaitlist_addsEntry() {
        String eventKey = "event123";
        String userId = "userABC";

        assertFalse(eventController.isOnWaitlist(eventKey, userId));

        eventController.addToWaitlist(eventKey, userId);

        List<WaitlistEntry> entries = eventController.getLotteryEntries();
        assertTrue(entries.stream().anyMatch(e ->
                eventKey.equals(e.getEventId())
                        && userId.equals(e.getUserId())
                        && e.getStatus() == Status.WAITLISTED
        ));
    }

    @Test
    // Verifies the “decline invite” behavior:
    // if a user is on the waitlist and addDeclinedEvent(eventId, userId) is called,
    // the controller should remove them from the waitlist and then record that declined result in the history list with status DECLINED

    public void decline_movesToHistory() {
        String eventKey = "event456";
        String userId = "userXYZ";

        // Start with user on waitlist
        eventController.addToWaitlist(eventKey, userId);
        assertTrue(eventController.isOnWaitlist(eventKey, userId));

        // Decline the event
        eventController.addDeclinedEvent(eventKey, userId);

        // User should be removed from waitlist
        assertFalse(eventController.isOnWaitlist(eventKey, userId));

        // And an entry should be present in history with DECLINED status
        List<WaitlistEntry> history = eventController.getHistoryEntries();
        assertTrue(history.stream().anyMatch(e ->
                eventKey.equals(e.getEventId())
                        && userId.equals(e.getUserId())
                        && e.getStatus() == Status.DECLINED
        ));
    }
}

