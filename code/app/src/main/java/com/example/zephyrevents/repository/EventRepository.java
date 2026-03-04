package com.example.zephyrevents.repository;

import com.example.zephyrevents.model.Event;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;



public class EventRepository {

    private final FirebaseFirestore db;

    public EventRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void createEvent(Event event, RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
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
