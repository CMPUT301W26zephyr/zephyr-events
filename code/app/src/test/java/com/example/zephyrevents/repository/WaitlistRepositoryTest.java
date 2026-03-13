package com.example.zephyrevents.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

// Anthropic, Claude, "Write Mockito unit tests for WaitlistRepository", 2026-03-12

public class WaitlistRepositoryTest {

    private WaitlistRepository repository;
    private RepositoryCallback<Void> mockVoidCallback;
    private RepositoryCallback<List<WaitlistEntry>> mockListCallback;

    @Before
    public void setup() {
        mockVoidCallback = mock(RepositoryCallback.class);
        mockListCallback = mock(RepositoryCallback.class);
        // use injected constructor so no real Firebase connection
        repository = new WaitlistRepository(mock(FirebaseFirestore.class));
    }

    @Test
    public void addUserToWaitlist_nullEntry_callsOnFailure() {
        repository.addUserToWaitlist(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void addUserToWaitlist_nullEntry_neverCallsOnSuccess() {
        repository.addUserToWaitlist(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void selectRandomEntrants_respectsCapacity() {
        // Override getWaitlist so we can test the pure lottery logic without touching Firestore.
        WaitlistRepository repoWithStubbedWaitlist = new WaitlistRepository(mock(FirebaseFirestore.class)) {
            @Override
            public void getWaitlist(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
                List<WaitlistEntry> entries = new ArrayList<>();
                entries.add(new WaitlistEntry("u1", eventId, 0.0, 0.0, Status.WAITLISTED));
                entries.add(new WaitlistEntry("u2", eventId, 0.0, 0.0, Status.WAITLISTED));
                entries.add(new WaitlistEntry("u3", eventId, 0.0, 0.0, Status.WAITLISTED));
                callback.onSuccess(entries);
            }
        };

        ArgumentCaptor<List<WaitlistEntry>> captor = ArgumentCaptor.forClass(List.class);

        repoWithStubbedWaitlist.selectRandomEntrants("event123", 2, mockListCallback);

        verify(mockListCallback).onSuccess(captor.capture());
        List<WaitlistEntry> winners = captor.getValue();

        // Capacity is 2, so we should never receive more than 2 winners.
        assertEquals(2, winners.size());

        // All returned winners should be from the same event.
        assertTrue(winners.stream().allMatch(e -> "event123".equals(e.getEventId())));
    }
}

