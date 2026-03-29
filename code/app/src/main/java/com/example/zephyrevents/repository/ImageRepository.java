package com.example.zephyrevents.repository;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Upload profile images to the Firebase Storage. Path: users/{userId}/avatar.jpg
 */

public class ImageRepository {
    private final FirebaseStorage storage;

    public ImageRepository(){
        this.storage = FirebaseStorage.getInstance();
    }

    public ImageRepository(FirebaseStorage storage){
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

    /**
     * Upload event poster to the firebase storage. Path: events/{eventId}/poster.jpg
     */

    public void uploadEventImage(Uri localUri, String eventId, RepositoryCallback<String> callback){
        if (eventId == null || eventId.trim().isEmpty()){
            callback.onFailure(new IllegalArgumentException("Invalid event id"));
            return;
        }
        if (localUri == null){
            callback.onFailure(new IllegalArgumentException("Uri is null"));
            return;
        }

        StorageReference ref = storage.getReference()
                .child("events")
                .child(eventId)
                .child("poster.jpg");
        ref.putFile(localUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);



    }




}
