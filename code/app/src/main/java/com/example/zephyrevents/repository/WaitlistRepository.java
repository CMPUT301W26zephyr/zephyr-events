package com.example.zephyrevents.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.WaitlistEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaitlistRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "WaitlistRepository";

    public WaitlistRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // CREATE - add user to Waitlist
    public void addUserToWaitlist(WaitlistEntry entry, RepositoryCallback<Void> callback) {

        if (entry == null) {
            callback.onFailure(new IllegalArgumentException("WaitlistEntry cannot be null"));
            return;
        }

        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .add(entry)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    // READ - get all waitlist of an event
    public void getWaitlist(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {

        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<WaitlistEntry> entries = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        WaitlistEntry entry = doc.toObject(WaitlistEntry.class);
                        entries.add(entry);
                    }

                    callback.onSuccess(entries);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // UPDATE - change waitlist status like success and fail
    public void updateStatus(String eventId, String userId, Status status, RepositoryCallback<Void> callback) {

        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().update("status", status);
                    }

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // DELETE - remove from waitlist
    public void removeUserFromWaitlist(String eventId, String userId, RepositoryCallback<Void> callback) {

        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // LOTTERY HELPER -gets waitlist - shuffles entries - returns first capacity users
    public void selectRandomEntrants(String eventId, int capacity,
                                     RepositoryCallback<List<WaitlistEntry>> callback) {

        getWaitlist(eventId, new RepositoryCallback<List<WaitlistEntry>>() {

            @Override
            public void onSuccess(List<WaitlistEntry> entrants) {

                List<WaitlistEntry> shuffled = new ArrayList<>(entrants);
                Collections.shuffle(shuffled);

                List<WaitlistEntry> winners =
                        shuffled.subList(0, Math.min(capacity, shuffled.size()));

                callback.onSuccess(winners);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // READ - get a specific user's entry for an event
    public void getUserWaitlistEntry(String eventId, String userId, RepositoryCallback<WaitlistEntry> callback) {
        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        callback.onSuccess(querySnapshot.getDocuments().get(0).toObject(WaitlistEntry.class));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // READ - get all waitlists joined by a specific user
    public void getWaitlistsForUser(String userId, RepositoryCallback<List<WaitlistEntry>> callback) {
        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitlistEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        entries.add(doc.toObject(WaitlistEntry.class));
                    }
                    callback.onSuccess(entries);
                })
                .addOnFailureListener(callback::onFailure);
    }


}