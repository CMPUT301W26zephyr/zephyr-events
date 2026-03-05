package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;
import java.util.List;
import java.util.Objects;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;


/*
    Gonna add better documentation later.
    Here are some refs I used. def recommend reading if you are interested!
    https://firebase.google.com/docs/firestore/manage-data/add-data#java_2
    https://firebase.google.com/docs/database/admin/retrieve-data#java
    https://developer.android.com/reference/android/util/Log
 */

public class EventRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "EventRepository";

    public EventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveEvent(Event event, RepositoryCallback<Void> callback) {
        if (event == null) {
            var e = new IllegalArgumentException("Event cannot be null");
            Log.w(TAG, "event passed had value of null", e);
            callback.onFailure(e);
            return;
        }
        if (event.getId() == null || event.getId().trim().isEmpty()){
            var e = new IllegalArgumentException("Event id passed has no value");
            Log.w(TAG, "event has no associated id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.EVENTS)
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore event added object id: " + event.getId());
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

    public void createEvent(Event event, RepositoryCallback<Void> callback){
        saveEvent(event, callback);
    }

    public void updateEvent(Event event, RepositoryCallback<Void> callback) {
        saveEvent(event, callback);
    }


    public void getEventById(String id, RepositoryCallback<Event> callback) {
        //TODO: FIREBASE CODE
    }


    public void deleteEvent(String id, RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    /* Pretty sure this is going to be a continuous stream, not a one shot callback.
     Maybe look into recycler view for this, remember to tell team.
     Maybe look into streaming chunks at a time into mem
     and setting limits on how many can come in at a time.
     */
    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        //TODO: FIREBASE CODE
    }
}
