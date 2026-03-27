package com.example.zephyrevents.repository;

import android.net.Uri;
import androidx.annotation.VisibleForTesting;

import com.google.firebase.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Upload profile images to the Firebase Storage. Path: users/{userId}/avatar.jpg
 */

public class ProfileImageRepository {
    private final FirebaseStorage storage;

    public ProfileImageRepository(){
        this.storage = FirebaseStorage.getInstance();
    }

    public ProfileImageRepository(FirebaseStorage storage){
        this.storage = storage;
    }

    public void uploadProfileImage(Uri localUri, String userId, RepositoryCallback<String> callback){
        if (userId == null || userId.trim().isEmpty()){
            callback.onFailure(new IllegalArgumentException("Invalid user id"));
            return;

        }
        if (localUri == null){
            callback.onFailure(new IllegalArgumentException("Uri is null"));
            return;
        }
        StorageReference ref = storage.getReference()
                .child("users")
                .child(userId)
                .child("avatar.jpg");
        ref.putFile(localUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);

    }




}
