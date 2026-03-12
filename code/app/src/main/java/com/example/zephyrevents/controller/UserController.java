package com.example.zephyrevents.controller;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import java.util.UUID;

public class UserController {

    private final UserRepository userRepository;
    private final SharedPreferences prefs;

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_USER_ID = "current_user_id";

    /**
     * Public constructor
     */
    public UserController(Context context) {
        this.userRepository = new UserRepository();
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if a user is currently logged in (tracked locally).
     */
    public boolean isUserLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    /**
     * Retrieves the local User ID.
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Handles the Sign-Up logic.
     */
    public void signUp(String name, String email, String phone, RepositoryCallback<Void> callback) {
        String newUserId = UUID.randomUUID().toString();

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(email);
        contactInfo.setPhone(phone);

        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setName(name);
        newUser.setContactInfo(contactInfo);
        newUser.setNotificationsOptOut(false);

        userRepository.createUser(newUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                prefs.edit().putString(KEY_USER_ID, newUserId).apply();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Fetches the full User object from Firebase.
     * Logs
     */
    public void fetchCurrentUser(RepositoryCallback<User> callback) {
        String userId = getCurrentUserId();
        if (userId != null) {
            userRepository.getUserById(userId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(Exception e) {
                    // If the doc is gone, the local state is out of sync, must clear it
                    if (e.getMessage() != null && e.getMessage().contains("document returned doesn't exist")) {
                        forceLogOut();
                    }
                    callback.onFailure(e);
                }
            });
        } else {
            callback.onFailure(new Exception("No local user logged in."));
        }
    }

    /**
     * Deletes the current user's account from Firebase AND clears local session.
     */
    public void deleteAccount(RepositoryCallback<Void> callback) {
        String userId = getCurrentUserId();

        if (userId == null) {
            callback.onFailure(new Exception("No user logged in locally."));
            return;
        }

        // IMPORTANT: Clear the local preference so they are "logged out"
        // done in advance because firebase enqueues the request upon network failure
        prefs.edit().remove(KEY_USER_ID).apply();

        userRepository.deleteUser(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Account logged out locally, server deletion queued."));
            }
        });
    }

    public void forceLogOut() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }

}