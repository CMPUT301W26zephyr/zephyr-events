package com.example.zephyrevents.repository;

import com.example.zephyrevents.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

//consider using collection.
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void createUser(User user, RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }

    public void getUserById(String id, RepositoryCallback<User> callback) {
        // TODO: FIREBASE CODE
    }

    public void updateUser(User user, RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }

    public void deleteUser(String id, RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }

    public void updateNotificationOptOut(String userId,
                                         boolean optOut,
                                         RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }
}