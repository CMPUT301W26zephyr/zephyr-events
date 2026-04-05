package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;

// Anthropic, Claude, "Write Mockito unit tests for ImageRepository", 2026-04-05

public class ImageRepositoryTest {

    private ImageRepository repository;
    private RepositoryCallback<String> mockStringCallback;
    private RepositoryCallback<Void> mockVoidCallback;

    private StorageReference mockRootRef;
    private StorageReference mockChildRef;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockStringCallback = mock(RepositoryCallback.class);
        mockVoidCallback   = mock(RepositoryCallback.class);

        mockRootRef  = mock(StorageReference.class);
        mockChildRef = mock(StorageReference.class);

        Task<Void> mockVoidTask       = (Task<Void>) mock(Task.class);
        UploadTask mockUploadTask     = mock(UploadTask.class);
        FirebaseStorage mockStorage   = mock(FirebaseStorage.class);

        when(mockStorage.getReference()).thenReturn(mockRootRef);
        when(mockRootRef.child(anyString())).thenReturn(mockChildRef);
        when(mockChildRef.child(anyString())).thenReturn(mockChildRef);

        when(mockChildRef.putFile(any())).thenReturn(mockUploadTask);
        when(mockUploadTask.addOnSuccessListener(any())).thenReturn(mockUploadTask);
        when(mockUploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask);

        when(mockChildRef.delete()).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);

        repository = new ImageRepository(mockStorage);
    }

    // --- uploadProfileImage ---

    @Test
    public void uploadProfileImage_nullUserId_callsOnFailure() {
        repository.uploadProfileImage(mock(Uri.class), null, mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadProfileImage_emptyUserId_callsOnFailure() {
        repository.uploadProfileImage(mock(Uri.class), "", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadProfileImage_whitespaceUserId_callsOnFailure() {
        repository.uploadProfileImage(mock(Uri.class), "   ", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadProfileImage_nullUri_callsOnFailure() {
        repository.uploadProfileImage(null, "user-123", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadProfileImage_nullUserId_neverCallsOnSuccess() {
        repository.uploadProfileImage(mock(Uri.class), null, mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void uploadProfileImage_nullUri_neverCallsOnSuccess() {
        repository.uploadProfileImage(null, "user-123", mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void uploadProfileImage_validArgs_neverCallsOnFailureImmediately() {
        repository.uploadProfileImage(mock(Uri.class), "user-123", mockStringCallback);
        verify(mockStringCallback, never()).onFailure(any());
    }

    // --- uploadEventImage ---

    @Test
    public void uploadEventImage_nullEventId_callsOnFailure() {
        repository.uploadEventImage(mock(Uri.class), null, mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadEventImage_emptyEventId_callsOnFailure() {
        repository.uploadEventImage(mock(Uri.class), "", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadEventImage_whitespaceEventId_callsOnFailure() {
        repository.uploadEventImage(mock(Uri.class), "   ", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadEventImage_nullUri_callsOnFailure() {
        repository.uploadEventImage(null, "event-123", mockStringCallback);
        verify(mockStringCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void uploadEventImage_nullEventId_neverCallsOnSuccess() {
        repository.uploadEventImage(mock(Uri.class), null, mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void uploadEventImage_nullUri_neverCallsOnSuccess() {
        repository.uploadEventImage(null, "event-123", mockStringCallback);
        verify(mockStringCallback, never()).onSuccess(any());
    }

    @Test
    public void uploadEventImage_validArgs_neverCallsOnFailureImmediately() {
        repository.uploadEventImage(mock(Uri.class), "event-123", mockStringCallback);
        verify(mockStringCallback, never()).onFailure(any());
    }

    // --- deleteProfileAvatar ---

    @Test
    public void deleteProfileAvatar_nullUserId_callsOnFailure() {
        repository.deleteProfileAvatar(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteProfileAvatar_emptyUserId_callsOnFailure() {
        repository.deleteProfileAvatar("", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteProfileAvatar_whitespaceUserId_callsOnFailure() {
        repository.deleteProfileAvatar("   ", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteProfileAvatar_nullUserId_neverCallsOnSuccess() {
        repository.deleteProfileAvatar(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void deleteProfileAvatar_validUserId_neverCallsOnFailureImmediately() {
        repository.deleteProfileAvatar("user-123", mockVoidCallback);
        verify(mockVoidCallback, never()).onFailure(any());
    }

    // --- deleteEventPoster ---

    @Test
    public void deleteEventPoster_nullEventId_callsOnFailure() {
        repository.deleteEventPoster(null, mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteEventPoster_emptyEventId_callsOnFailure() {
        repository.deleteEventPoster("", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteEventPoster_whitespaceEventId_callsOnFailure() {
        repository.deleteEventPoster("   ", mockVoidCallback);
        verify(mockVoidCallback).onFailure(any(IllegalArgumentException.class));
    }

    @Test
    public void deleteEventPoster_nullEventId_neverCallsOnSuccess() {
        repository.deleteEventPoster(null, mockVoidCallback);
        verify(mockVoidCallback, never()).onSuccess(any());
    }

    @Test
    public void deleteEventPoster_validEventId_neverCallsOnFailureImmediately() {
        repository.deleteEventPoster("event-123", mockVoidCallback);
        verify(mockVoidCallback, never()).onFailure(any());
    }
}