package com.example.zephyrevents.repository;

import com.example.zephyrevents.model.EventStatus;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;

public class WaitlistRepository {
    private final FirebaseFirestore db;
    public WaitlistRepository() {
        db = FirebaseFirestore.getInstance();
    }
    public void addUserToWaitlist(String eventId, String userId,
                                  RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    public void updateStatus(String eventId, String userId,
                             EventStatus status,
                             RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    // consider waitlist to be some model named "WaitlistEntry"
    //so it is not a list.
    public void getWaitlist(String eventId,
                            //this one
                            RepositoryCallback<List<String>> callback) {
        //TODO: FIREBASE CODE
    }

    public void selectRandomEntrants(String eventId, int capacity,
                                     RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }
}
