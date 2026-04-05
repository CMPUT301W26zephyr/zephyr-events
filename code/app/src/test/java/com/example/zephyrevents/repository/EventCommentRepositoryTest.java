package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.zephyrevents.model.EventComment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

// Anthropic, Claude, "Write Mockito unit tests for EventCommentRepository", 2026-04-05

public class EventCommentRepositoryTest {

    private EventCommentRepository repository;
    private RepositoryCallback<String> mockStringCallback;
    private RepositoryCallback<Void> mockVoidCallback;
    private RepositoryCallback<List<EventComment>> mockListCallback;

    private CollectionReference mockCollection;
    private Query mockQuery;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockStringCallback = mock(RepositoryCallback.class);
        mockVoidCallback   = mock(RepositoryCallback.class);
        mockListCallback   = mock(RepositoryCallback.class);

        mockCollection = mock(CollectionReference.class);
        mockQuery      = mock(Query.class);

        Task<Void> mockVoidTask         = (Task<Void>) mock(Task.class);
        FirebaseFirestore mockDb        = mock(FirebaseFirestore.class);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.whereEqualTo(anyString(), any())).thenReturn(mockQuery);
        when(mockCollection.document(anyString())).thenReturn(mock(com.google.firebase.firestore.DocumentReference.class));

        when(mockQuery.get()).thenReturn((Task) mockVoidTask);
        when(mockQuery.addSnapshotListener(any())).thenReturn(mock(com.google.firebase.firestore.ListenerRegistration.class));

        when(mockCollection.add(any())).thenReturn((Task) mockVoidTask);
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);

        repository = new EventCommentRepository(mockDb);
    }

    // --- addComment ---

    @Test
    public void addComment_nullEventId_callsOnFailure() {
        EventComment comment = new EventComment();
        comment.setEventId(null);
        comment.setUserId("user-123");
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void addComment_nullUserId_callsOnFailure() {
        EventComment comment = new EventComment();
        comment.setEventId("event-123");
        comment.setUserId(null);
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void addComment_nullEventIdAndUserId_callsOnFailure() {
        EventComment comment = new EventComment();
        comment.setEventId(null);
        comment.setUserId(null);
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void addComment_nullEventId_neverCallsOnSuccess() {
        EventComment comment = new EventComment();
        comment.setEventId(null);
        comment.setUserId("user-123");
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void addComment_nullUserId_neverCallsOnSuccess() {
        EventComment comment = new EventComment();
        comment.setEventId("event-123");
        comment.setUserId(null);
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void addComment_validComment_neverCallsOnFailureImmediately() {
        EventComment comment = new EventComment();
        comment.setEventId("event-123");
        comment.setUserId("user-123");
        repository.addComment(comment, mockStringCallback);
        verify(mockStringCallback, never()).onFailure(any());
    }

    // --- listenToEventComments ---

    @Test
    public void listenToEventComments_validArgs_neverCallsOnFailureImmediately() {
        repository.listenToEventComments("event-123", mockListCallback);
        verify(mockListCallback, never()).onFailure(any());
    }

    @Test
    public void listenToEventComments_validArgs_neverCallsOnSuccessImmediately() {
        repository.listenToEventComments("event-123", mockListCallback);
        verify(mockListCallback, never()).onSuccess(any());
    }

    // --- deleteCommentAndReplies ---

    @Test
    public void deleteCommentAndReplies_validId_neverCallsOnFailureImmediately() {
        repository.deleteCommentAndReplies("comment-123", mockVoidCallback);
        verify(mockVoidCallback, never()).onFailure(any());
    }

    @Test
    public void deleteCommentAndReplies_validId_neverCallsOnSuccessImmediately() {
        repository.deleteCommentAndReplies("comment-123", mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }
}
