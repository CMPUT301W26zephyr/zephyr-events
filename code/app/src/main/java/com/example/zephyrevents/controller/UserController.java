package com.example.zephyrevents.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.ContactInfo;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import java.util.UUID;
import android.net.Uri;
import com.example.zephyrevents.repository.ImageRepository;



/**
 * Controller that manages logic related to users, user profiles, and session state.
 * Handles firebase updates through UserRepository, and local session with SharedPreferences
 */
public class UserController {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    private final SharedPreferences prefs;

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_USER_ID = "current_user_id";

    /**
     * Public constructor for use within Android Activities or Fragments.
     *
     * @param context Used to initialize SharedPreferences.
     */
    public UserController(Context context) {
        this.userRepository = new UserRepository();
        this.imageRepository = new ImageRepository();
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    }

    /**
     * Constructor used for Unit Testing.
     *
     * @param userRepository A mocked or custom {@link UserRepository}.
     * @param prefs A mocked or custom {@link SharedPreferences}.
     */
    @androidx.annotation.VisibleForTesting
    public UserController(UserRepository userRepository, SharedPreferences prefs) {
        this.userRepository = userRepository;
        this.imageRepository = null;
        this.prefs = prefs;
    }

    /**
     * Checks if a user is currently logged in (tracked locally).
     *
     * @return true if user ID exists in SharedPreferences, otherwise false.
     */
    public boolean isUserLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    /**
     * Retrieves the local User ID.
     *
     * @return The user ID string if user signed in, otherwise null.
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Handles the Sign-Up logic for a new ueser.
     *
     * @param name      The user's name
     * @param email     The user's email
     * @param phone     The user's phone number
     * @param callback  A RepositoryCallback to handle success or failure
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
     * Fetches the full User object from Firebase, for the current user logged in.
     * If the remote document is missing (e.g. deleted by admin), triggers forceLogOut() immediately.
     *
     * @param callback  A RepositoryCallback to handle success or failure
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
     * Deletes the current user's account from Firebase and clears local session.
     * Note: local session cleared immediately even if network call fails.
     *
     * @param callback  A RepositoryCallback to handle success or failure
     */
    public void deleteAccount(RepositoryCallback<Void> callback) {
        String userId = getCurrentUserId();

        if (userId == null) {
            callback.onFailure(new Exception("No user logged in locally."));
            return;
        }

        // Clear local preference to log out: done in advance because firebase enqueues the request upon network failure
        forceLogOut();

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

    /**
     * Clears the current user's session from local storage. Does not affect Firestore data.
     */
    public void forceLogOut() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }


    /**
     * Updates the current user's profile in Firestore.
     * Fetches the existing User object, modifies requested fields, and then saves back to repository.

     * @param name      The user's name
     * @param email     The user's email
     * @param phone     The user's phone number
     * @param country   The user's location/country
     * @param callback  A RepositoryCallback to handle success or failure
     */
    public void updateCurrentUserProfile(
            String name,
            String email,
            String phone,
            String country,
            RepositoryCallback<Void> callback
    ) {
        fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onFailure(new Exception("User not loaded."));
                    return;
                }

                user.setName(name);
                user.setLocation(country);

                ContactInfo ci = user.getContactInfo();
                if (ci == null) ci = new ContactInfo();
                ci.setEmail(email);
                ci.setPhone(phone);
                user.setContactInfo(ci);

                userRepository.saveUser(user, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Convenience: Retrieves profile info of current session's user as a String array
     * Order: 0: Name, 1: Email, 2: Phone; 3: Country, 4: AvatarUrl
     *
     * @param callback  A RepositoryCallback to handle success or failure
     */
    public void getCurrentUserProfileInfo(RepositoryCallback<String[]> callback) {
        fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onFailure(new Exception("User not loaded."));
                    return;
                }
                ContactInfo ci = user.getContactInfo();
                String name = user.getName() == null ? "" : user.getName();
                String email = (ci != null && ci.getEmail() != null) ? ci.getEmail() : "";
                String phone = (ci != null && ci.getPhone() != null) ? ci.getPhone() : "";
                String country = user.getLocation() == null ? "" : user.getLocation();
                String avatarUrl = user.getAvatarUrl() == null ? "" : user.getAvatarUrl();
                callback.onSuccess(new String[]{ name, email, phone, country, avatarUrl });
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void updateProfileImg(Uri imageUri, RepositoryCallback<Void> callback){
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure(new Exception("No user found"));
            return;
        }
        if(imageRepository == null){
            callback.onFailure(new Exception("Profile image not available"));
            return;
        }
        imageRepository.uploadProfileImage(imageUri, userId, new RepositoryCallback<String>(){
            @Override
            public void onSuccess(String downloadUrl){
                fetchCurrentUser(new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        if (result == null){
                            callback.onFailure(new Exception("User not found"));
                            return;
                        }
                        result.setAvatarUrl(downloadUrl);
                        userRepository.saveUser(result, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);

                            }
                        });

                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);

                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }

        });
    }

    /**
     * Clear avatar in Firestore
     */

    public void clearProfileAvatar(RepositoryCallback<Void> callback){
        String userId = getCurrentUserId();
        if (userId == null){
            callback.onFailure(new Exception("No user found"));
            return;
        }

        fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (result == null){
                    callback.onFailure(new Exception("User is not loaded"));
                    return;
                }

                result.setAvatarUrl(null);
                userRepository.saveUser(result, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (imageRepository != null){
                            imageRepository.deleteProfileAvatar(userId, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(null);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    callback.onSuccess(null);

                                }
                            });
                        } else{
                            callback.onSuccess(null);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);

            }
        });
    }
}