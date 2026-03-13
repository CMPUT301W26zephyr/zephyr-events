package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;

import java.util.ArrayList;
import java.util.List;

import com.example.zephyrevents.util.TimeHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

    public EventRepository(FirebaseFirestore db) {
        this.db = db;
    }
    public void saveEvent(Event event, RepositoryCallback<Void> callback) {
        if (event == null) {
            var e = new IllegalArgumentException("Event cannot be null");
            Log.w(TAG, "event passed had value of null", e);
            callback.onFailure(e);
            return;
        }
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()){
            var e = new IllegalArgumentException("Event id passed has no value");
            Log.w(TAG, "event has no associated id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.EVENTS)
                .document(event.getEventId())
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Firestore event added object id: " + event.getEventId());
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
        if (id == null || id.trim().isEmpty()){
            var e = new IllegalArgumentException("event id passed has no value");
            Log.w(TAG, "invalid event id", e);
            callback.onFailure(e);
            return; // exit before network call.
        }
        db.collection(Collections.EVENTS)
                .document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        // is onSuccess guaranteed to be not null? Idk tbh.
                        if (doc == null || !doc.exists()){
                            var e = new IllegalArgumentException("document returned doesn't exist");
                            Log.w(TAG, "event document not found for id: "+id, e);
                            callback.onFailure(e);
                        }else{
                                Log.d(TAG, "DocumentSnapshot data: " + doc.getData());
                                callback.onSuccess(doc.toObject(Event.class));
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


    public void deleteEvent(String eventId, RepositoryCallback<Void> callback) {
        db.collection(Collections.EVENTS).document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    // Cascade delete associated waitlists
                    db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                            .whereEqualTo("eventId", eventId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot doc : querySnapshot) {
                                    doc.getReference().delete();
                                }
                            });

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /*

     */
    public void getAllEvents(RepositoryCallback<List<Event>> callback){
        db.collection(Collections.EVENTS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {


                        List<Event> events = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }

                        callback.onSuccess(events);
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error getting events", e);
                            callback.onFailure(e);
                        }
                    });
    }

    public void getOpenEvents(RepositoryCallback<List<Event>> callback){
        db.collection(Collections.EVENTS)
                .whereGreaterThan("time.endTime", TimeHelper.now())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {


                        List<Event> events = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }

                        callback.onSuccess(events);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error getting events", e);
                        callback.onFailure(e);
                    }
                });
    }
       public void getUpcomingEvents(RepositoryCallback<List<Event>> callback){
        db.collection(Collections.EVENTS)
                .whereGreaterThan("time.startTime", TimeHelper.now())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {


                        List<Event> events = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }

                        callback.onSuccess(events);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error getting events", e);
                        callback.onFailure(e);
                    }
                });
    }
    public void getEventsHappeningNow(RepositoryCallback<List<Event>> callback){
        db.collection(Collections.EVENTS)
                .whereLessThanOrEqualTo("time.startTime", TimeHelper.now())
                .whereGreaterThanOrEqualTo("time.endTime", TimeHelper.now())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {


                        List<Event> events = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Event event = doc.toObject(Event.class);
                            events.add(event);
                        }

                        callback.onSuccess(events);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error getting events", e);
                        callback.onFailure(e);
                    }
                });
    }
}