package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.Event;

import java.util.ArrayList;
import java.util.List;

import com.example.zephyrevents.util.TimeHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.N;


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

    /*
    Yeah, this is problematic.
    classic potential race condition :(
    This is actually a pretty good learning moment
    here you set the whole object when wanting to just update the event.
    seems pretty reasonable, but then, say you have two users and the following class

    public class user{
        private String name;
        private int grade;
        private int age;
    }

    User("name", "b", "67") // The base user they want to change

    Say one user wants to update the name
    User("foobar", "b", "67"); // name changes here

    and another wants to update the age
    User("name", "b", "12") // age changes here

    and then both clients use this updateEvent method
    who ever reaches first gets overwritten
    you will either have the age changed or the name changed.
    not both, and that undermines what the updateEvent method promises.
    so this is, badly written code by me lol. I wrote it in the middle of the night
    so ill fix it later, but I was on the bus and was just like, that's a perfect
    example of a race condition that might not be obvious to someone.
    it wasn't obvious to me last night until I thought about it more.

    Honestly for now we are not gonna run into a race condition, Imma just leave it for now.
     */
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