package com.example.zephyrevents.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.zephyrevents.model.ContactInfo;
import org.mockito.ArgumentCaptor;

import android.content.SharedPreferences;

import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

// Gemini 3.1 Pro Preview, Google AiStudio, "<code> How would you recommend testing this class?." - 2023-03-12

public class UserControllerTest {

    private UserRepository mockRepo;
    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;
    private UserController userController;

    @Before
    public void setUp() {
        mockRepo = Mockito.mock(UserRepository.class);
        mockPrefs = Mockito.mock(SharedPreferences.class);
        mockEditor = Mockito.mock(SharedPreferences.Editor.class);

        // Setup SharedPreferences mock behavior
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);

        // Inject mocks into the controller
        userController = new UserController(mockRepo, mockPrefs);
    }

    @Test
    // Verifies that isUserLoggedIn returns true when the SharedPreferences contain the key
    public void testIsUserLoggedIn_ReturnsTrue_WhenPrefsContainsId() {
        when(mockPrefs.contains("current_user_id")).thenReturn(true);
        assertTrue(userController.isUserLoggedIn());
    }

    @Test
    // Verifies that SharedPreferences contains the key after calling SignUp
    public void testSignUp_SavesUserLocally_OnSuccess() {
        RepositoryCallback<Void> mockCallback = Mockito.mock(RepositoryCallback.class);

        // 1. Intercept the call to mockRepo.createUser
        // When it is called, instantly trigger the onSuccess callback.
        doAnswer(invocation -> {
            RepositoryCallback<Void> callback = invocation.getArgument(1);
            callback.onSuccess(null); // Simulate Firebase success
            return null;
        }).when(mockRepo).createUser(any(User.class), any());

        // 2. Act: Call signUp
        userController.signUp("John", "john@test.com", "1234567890", mockCallback);

        // 3. Assert: Verify SharedPreferences was updated
        verify(mockEditor).putString(eq("current_user_id"), anyString());
        verify(mockEditor).apply();

        // Verify the original callback was notified of success
        verify(mockCallback).onSuccess(null);
    }

    @Test
    public void testForceLogOut_RemovesUserIdFromPrefs() {
        userController.forceLogOut();

        verify(mockEditor).remove("current_user_id");
        verify(mockEditor).apply();
    }

    @Test
    // Tests whether the user can edit their profile information
    public void testUpdateCurrentUserProfile(){
        when(mockPrefs.getString(eq("current_user_id"), any())).thenReturn("test-user-id");

        User existinguser = new User();
        existinguser.setId("test-user-id");
        existinguser.setName("John Doe");
        existinguser.setLocation("Canada");
        ContactInfo existingcontact = new ContactInfo();
        existingcontact.setEmail("johndoe@gmail.com");
        existingcontact.setPhone("123");
        existinguser.setContactInfo(existingcontact);

        doAnswer(invocation -> {
            RepositoryCallback<User> callback = invocation.getArgument(1);
            callback.onSuccess(existinguser);
            return null;

        }).when(mockRepo).getUserById(eq("test-user-id"), any());

        doAnswer(invocation -> {
            RepositoryCallback<Void> saveCallback = invocation.getArgument(1);
            saveCallback.onSuccess(null);
            return null;
        }).when(mockRepo).saveUser(any(User.class), any());

        RepositoryCallback<Void> callback = Mockito.mock(RepositoryCallback.class);
        userController.updateCurrentUserProfile("new name", "new@gmail.com", "000", "USA",callback);

        verify(callback).onSuccess(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockRepo).saveUser(userCaptor.capture(), any());
        User savedUser = userCaptor.getValue();
        assertTrue("new name".equals(savedUser.getName()));
        assertTrue("USA".equals(savedUser.getLocation()));
        assertTrue("new@gmail.com".equals(savedUser.getContactInfo().getEmail()));
        assertTrue("000".equals(savedUser.getContactInfo().getPhone()));

    }
}