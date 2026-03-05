package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;
import java.util.List;
import java.util.Objects;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;



public class EventRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "EventRepository";

    public EventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void createEvent(Event event, RepositoryCallback<Void> callback) {
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

    public void getEventById(String id, RepositoryCallback<Event> callback) {
        //TODO: FIREBASE CODE
    }

    public void updateEvent(Event event, RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    public void deleteEvent(String id, RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    public void getAllEvents(RepositoryCallback<List<Event>> callback) {
        //TODO: FIREBASE CODE
    }
}
