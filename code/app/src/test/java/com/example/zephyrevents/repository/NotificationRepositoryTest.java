package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.zephyrevents.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

// Anthropic, Claude, "Write Mockito unit tests for NotificationRepository", 2026-03-12

public class NotificationRepositoryTest {

    private NotificationRepository repository;
    private RepositoryCallback<Void> mockVoidCallback;
    private RepositoryCallback<Notification> mockNotificationCallback;
    private RepositoryCallback<List<Notification>> mockNotificationListCallback;

    @Before
    public void setup() {
        mockVoidCallback = mock(RepositoryCallback.class);
        mockNotificationCallback = mock(RepositoryCallback.class);
        mockNotificationListCallback = mock(RepositoryCallback.class);
        // use injected constructor so no real Firebase connection
        repository = new NotificationRepository(mock(FirebaseFirestore.class));
    }

    @Test
    public void saveNotification_nullNotification_callsOnFailure() {
        repository.saveNotification(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveNotification_nullNotification_neverCallsOnSuccess() {
        repository.saveNotification(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void saveNotification_emptyId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId("");
        repository.saveNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveNotification_nullId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId(null);
        repository.saveNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveNotification_whitespaceId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId("   ");
        repository.saveNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getNotificationId_nullId_callsOnFailure() {
        repository.getNotificationId(null, mockNotificationCallback);
        verify(mockNotificationCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getNotificationId_emptyId_callsOnFailure() {
        repository.getNotificationId("", mockNotificationCallback);
        verify(mockNotificationCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getNotificationId_whitespaceId_callsOnFailure() {
        repository.getNotificationId("   ", mockNotificationCallback);
        verify(mockNotificationCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getNotificationId_nullId_neverCallsOnSuccess() {
        repository.getNotificationId(null, mockNotificationCallback);
        verify(mockNotificationCallback, never()).onSuccess(any());
    }

    @Test
    public void updateNotification_nullNotification_callsOnFailure() {
        repository.updateNotification(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void updateNotification_nullNotification_neverCallsOnSuccess() {
        repository.updateNotification(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void updateNotification_emptyId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId("");
        repository.updateNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void updateNotification_nullId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId(null);
        repository.updateNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void updateNotification_whitespaceId_callsOnFailure() {
        Notification n = new Notification();
        n.setNotificationId("   ");
        repository.updateNotification(n, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteNotification_nullId_callsOnFailure() {
        repository.deleteNotification(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteNotification_emptyId_callsOnFailure() {
        repository.deleteNotification("", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteNotification_whitespaceId_callsOnFailure() {
        repository.deleteNotification("   ", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteNotification_nullId_neverCallsOnSuccess() {
        repository.deleteNotification(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void getUserNotifications_nullUserId_callsOnFailure() {
        repository.getUserNotifications(null, mockNotificationListCallback);
        verify(mockNotificationListCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getUserNotifications_emptyUserId_callsOnFailure() {
        repository.getUserNotifications("", mockNotificationListCallback);
        verify(mockNotificationListCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getUserNotifications_whitespaceUserId_callsOnFailure() {
        repository.getUserNotifications("   ", mockNotificationListCallback);
        verify(mockNotificationListCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getUserNotifications_nullUserId_neverCallsOnSuccess() {
        repository.getUserNotifications(null, mockNotificationListCallback);
        verify(mockNotificationListCallback, never()).onSuccess(any());
    }
}

