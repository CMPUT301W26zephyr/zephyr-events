package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

//consider using collection.
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    private static final String TAG = "UserRepository";
    public void createUser(User user, RepositoryCallback<Void> callback) {
        saveUser(user, callback);
    }

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

    public void updateUser(User user, RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }

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
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "error deleting user with id: "+id+"\n exception returned: ", e);
                        callback.onFailure(e);
                    }
                });    }

    public void updateNotificationOptOut(String userId,
                                         boolean optOut,
                                         RepositoryCallback<Void> callback) {
        // TODO: FIREBASE CODE
    }
}