package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository class for managing User data in Firestore.
 * Provides methods for creating, reading, updating, and deleting users.
 */
public class UserRepository {

    private final FirebaseFirestore db;

    /**
     * Default constructor. Uses the production Firestore instance.
     */
    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor with dependency injection for testing.
     * @param db  The injected firestore instance.
     */
    public UserRepository(FirebaseFirestore db) {
        this.db = db;
    }

    private static final String TAG = "UserRepository";

    /**
     * Creates a new user in the database. Alias for SaveUser() unless implementation changes.
     * @param user      The user object to create.
     * @param callback  Callback to handle success or failure of the operation.
     */
    public void createUser(User user, RepositoryCallback<Void> callback) {
        saveUser(user, callback);
    }

    /**
     * Saves or updates a user in the Firestore "users" collection.
     * Validates that the user and user ID are not null or empty before writing.
     * @param user      The user object to create.
     * @param callback  Callback to handle success or failure of the operation.
     */
    public void saveUser(User user, RepositoryCallback<Void> callback) {
        if (user == null) {
            var e = new IllegalArgumentException("User cannot be null");
            Log.w(TAG, "User passed had value of null", e);
            callback.onFailure(e);
            return;
        }
        if (user.getId() == null || user.getId().trim().isEmpty()){
            var e = new IllegalArgumentException("user id passed has no value");
            Log.w(TAG, "user has no associated id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.USERS)
            .document(user.getId())
            .set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "firestore event added object id: " + user.getId());
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                    callback.onFailure(e);
                }
            });

    }

    /**
     * Retrieves a user by their unique document ID.
     * @param id        The unique ID of the user.
     * @param callback  Returns the User object if found, or error if the document does not exist.
     */
    public void getUserById(String id, RepositoryCallback<User> callback) {
        if (id == null || id.trim().isEmpty()){
            var e = new IllegalArgumentException("user id passed has no value");
            Log.w(TAG, "invalid user id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.USERS)
                .document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc == null || !doc.exists()){
                            var e = new IllegalArgumentException("document returned doesn't exist");
                            Log.w(TAG, "user document not found for id: "+id, e);
                            callback.onFailure(e);
                        }else{
                            Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                            callback.onSuccess(doc.toObject(User.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "error getting doc by id: "+id+"\n exception returned: ", e);
                        callback.onFailure(e);
                    }
                });

    }

    // Potential race condition, update later if needed.
    /**
     * Updates an existing user's information. Alias for SaveUser() unless implementation changes.
     * @param user      The user object to create.
     * @param callback  Callback to handle success or failure of the operation.
     */
    public void updateUser(User user, RepositoryCallback<Void> callback) {
        saveUser(user, callback);
    }

    /**
     * Deletes a User from Firestore by its ID.
     * Also performs a cascade delete of any waitlist entries for the user.
     * @param id        The ID of the user to delete.
     * @param callback  Handle completion of the user deletion.
     */
    public void deleteUser(String id, RepositoryCallback<Void> callback) {
        if (id == null || id.trim().isEmpty()){
            var e = new IllegalArgumentException("user id passed has no value");
            Log.w(TAG, "invalid user id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.USERS)
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "user deleted successfully id: " + id);

                        // Cascade delete associated waitlists
                        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                                .whereEqualTo("userId", id)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot doc : querySnapshot) {
                                        doc.getReference().delete();
                                    }
                                });

                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "error deleting user with id: "+id+"\n exception returned: ", e);
                        callback.onFailure(e);
                    }
                });
    }

    // Potential race condition, but... realistically how many people are updating user?
    /**
     * Updates the notification opt-out status for a specific user.
     * Checks if the current state matches the requested state to prevent unnecessary network calls.
     * @param user      the user object
     * @param userId    user ID
     * @param optOut    true to opt-out, false to opt-in.
     * @param callback  handles result
     */
    public void updateNotificationOptOut(User user, String userId,
                                         boolean optOut,
                                         RepositoryCallback<Void> callback) {
        if (user == null){
            var e = new IllegalArgumentException("user has no value");
            Log.w(TAG, "error user with id: "+userId+"\n exception returned: ", e);
            return; // return before network call
        }
        // break out before the network call if input param == data field.
        if (user.isNotificationsOptOut() == optOut){ return;}
        user.setNotificationsOptOut(optOut);
        saveUser(user, callback);
    }
}