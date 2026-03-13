package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.example.zephyrevents.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

// Anthropic, Claude, "Write Mockito unit tests for UserRepository.java for an Android Firebase app", 2026-03-12

public class UserRepositoryTest {

    private UserRepository repository;
    private RepositoryCallback<Void> mockVoidCallback;
    private RepositoryCallback<User> mockUserCallback;

    @Before
    public void setup() {
        mockVoidCallback = mock(RepositoryCallback.class);
        mockUserCallback = mock(RepositoryCallback.class);
        repository = new UserRepository(mock(FirebaseFirestore.class));
    }

    // --- saveUser ---

    @Test
    public void saveUser_nullUser_callsOnFailure() {
        repository.saveUser(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveUser_nullUser_neverCallsOnSuccess() {
        repository.saveUser(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void saveUser_nullUserId_callsOnFailure() {
        User user = new User();
        user.setId(null);
        repository.saveUser(user, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveUser_emptyUserId_callsOnFailure() {
        User user = new User();
        user.setId("");
        repository.saveUser(user, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void saveUser_whitespaceUserId_callsOnFailure() {
        User user = new User();
        user.setId("   ");
        repository.saveUser(user, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    // --- getUserById ---

    @Test
    public void getUserById_nullId_callsOnFailure() {
        repository.getUserById(null, mockUserCallback);
        verify(mockUserCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getUserById_nullId_neverCallsOnSuccess() {
        repository.getUserById(null, mockUserCallback);
        verify(mockUserCallback, never()).onSuccess(any());
    }

    @Test
    public void getUserById_emptyId_callsOnFailure() {
        repository.getUserById("", mockUserCallback);
        verify(mockUserCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void getUserById_whitespaceId_callsOnFailure() {
        repository.getUserById("   ", mockUserCallback);
        verify(mockUserCallback).onFailure(any(IllegalArgumentException.class));
    }

    // --- deleteUser ---

    @Test
    public void deleteUser_nullId_callsOnFailure() {
        repository.deleteUser(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteUser_emptyId_callsOnFailure() {
        repository.deleteUser("", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteUser_whitespaceId_callsOnFailure() {
        repository.deleteUser("   ", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    // --- updateNotificationOptOut ---

    @Test
    public void updateNotificationOptOut_nullUser_doesNotCallCallback() {
        repository.updateNotificationOptOut(null, "some-id", true, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
        verify(mockVoidCallback, never()).onFailure(any());
    }

    @Test
    public void updateNotificationOptOut_sameValue_doesNotCallCallback() {
        User user = new User();
        user.setId("some-id");
        user.setNotificationsOptOut(true);
        // passing same value as already set, should early return
        repository.updateNotificationOptOut(user, "some-id", true, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
        verify(mockVoidCallback, never()).onFailure(any());
    }
}