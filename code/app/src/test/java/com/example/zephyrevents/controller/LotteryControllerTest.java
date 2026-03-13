package com.example.zephyrevents.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.WaitlistRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

// Gemini 3.1 Pro Preview, Google AiStudio, "Please write tests for LotteryController using junit and mockito."

public class LotteryControllerTest {
    private EventRepository mockEventRepo;
    private WaitlistRepository mockWaitlistRepo;
    private RepositoryCallback<Void> mockFinalCallback;

    private LotteryController lotteryController;

    private static final String EVENT_ID = "event_123";

    @Before
    public void setUp() {
        mockEventRepo = Mockito.mock(EventRepository.class);
        mockWaitlistRepo = Mockito.mock(WaitlistRepository.class);
        mockFinalCallback = Mockito.mock(RepositoryCallback.class);
        lotteryController = new LotteryController(mockEventRepo, mockWaitlistRepo);
    }

    @Test
    // Verifies that controller passes Exception back if repository fails to retrieve Event
    public void testRunLottery_FailsIfEventNotFound() {
        // Arrange: Simulate getEventById failing
        doAnswer(invocation -> {
            RepositoryCallback<Event> callback = invocation.getArgument(1);
            callback.onFailure(new Exception("Event not found"));
            return null;
        }).when(mockEventRepo).getEventById(eq(EVENT_ID), any());

        lotteryController.runLottery(EVENT_ID, mockFinalCallback);

        // The final callback should receive the failure
        verify(mockFinalCallback).onFailure(any(Exception.class));
    }

    @Test
    // Verifies edge case:
    public void testRunLottery_SucceedsInstantlyIfNoEligibleEntrants() {
        // Arrange: Mock Event
        Event mockEvent = new Event();
        mockEvent.setCapacity(5);

        doAnswer(invocation -> {
            RepositoryCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepo).getEventById(eq(EVENT_ID), any());

        // Arrange: Mock Waitlist with NO eligible entrants (e.g., all already LOST)
        List<WaitlistEntry> entrants = new ArrayList<>();
        entrants.add(new WaitlistEntry("user1", EVENT_ID, 0, 0, Status.LOST));

        doAnswer(invocation -> {
            RepositoryCallback<List<WaitlistEntry>> callback = invocation.getArgument(1);
            callback.onSuccess(entrants);
            return null;
        }).when(mockWaitlistRepo).getWaitlist(eq(EVENT_ID), any());

        // Act
        lotteryController.runLottery(EVENT_ID, mockFinalCallback);

        // Assert: Success is called immediately, updateStatus is never called
        verify(mockFinalCallback).onSuccess(null);
        verify(mockWaitlistRepo, times(0)).updateStatus(anyString(), anyString(), any(), any());
    }

    @Test
    public void testRunLottery_SuccessfullySelectsWinnersAndLosers() {
        // Arrange: Mock Event with Capacity of 2
        Event mockEvent = new Event();
        mockEvent.setCapacity(2);

        doAnswer(invocation -> {
            RepositoryCallback<Event> callback = invocation.getArgument(1);
            callback.onSuccess(mockEvent);
            return null;
        }).when(mockEventRepo).getEventById(eq(EVENT_ID), any());

        // Arrange: Mock Waitlist with 3 Eligible Entrants
        List<WaitlistEntry> entrants = new ArrayList<>();
        entrants.add(new WaitlistEntry("user1", EVENT_ID, 0, 0, Status.WAITLISTED));
        entrants.add(new WaitlistEntry("user2", EVENT_ID, 0, 0, Status.WAITLISTED));
        entrants.add(new WaitlistEntry("user3", EVENT_ID, 0, 0, Status.WAITLISTED));

        doAnswer(invocation -> {
            RepositoryCallback<List<WaitlistEntry>> callback = invocation.getArgument(1);
            callback.onSuccess(entrants);
            return null;
        }).when(mockWaitlistRepo).getWaitlist(eq(EVENT_ID), any());

        // Arrange: Simulate updateStatus succeeding instantly so the counter tracks properly
        doAnswer(invocation -> {
            RepositoryCallback<Void> callback = invocation.getArgument(3);
            callback.onSuccess(null);
            return null;
        }).when(mockWaitlistRepo).updateStatus(anyString(), anyString(), any(Status.class), any());

        // Act
        lotteryController.runLottery(EVENT_ID, mockFinalCallback);

        // Assert: Verify updateStatus was called exactly 3 times in total
        verify(mockWaitlistRepo, times(3)).updateStatus(eq(EVENT_ID), anyString(), any(Status.class), any());

        // Assert: Capture the statuses passed to updateStatus to ensure 2 won and 1 lost
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);
        verify(mockWaitlistRepo, times(3)).updateStatus(eq(EVENT_ID), anyString(), statusCaptor.capture(), any());

        List<Status> updatedStatuses = statusCaptor.getAllValues();
        long winnersCount = updatedStatuses.stream().filter(s -> s == Status.SELECTED).count();
        long losersCount = updatedStatuses.stream().filter(s -> s == Status.LOST).count();

        assertEquals("There should be exactly 2 winners", 2, winnersCount);
        assertEquals("There should be exactly 1 loser", 1, losersCount);

        // Assert: The final callback was triggered indicating the loop finished
        verify(mockFinalCallback).onSuccess(null);
    }

}
