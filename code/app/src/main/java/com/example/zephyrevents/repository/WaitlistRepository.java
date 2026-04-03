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

/**
 * Repository class for managing WaitlistEntry data in Firebase Firestore.
 * This class handles the logic for users joining event waitlists,
 * and status updates (e.g., selected/rejected), and aids selection process.
 */
public class WaitlistRepository {

    private final FirebaseFirestore db;
    private static final String TAG = "WaitlistRepository";

    /**
     * Default constructor. Uses the production Firestore instance.
     */
    public WaitlistRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructor with dependency injection for testing.
     * @param db  The injected firestore instance.
     */
    public WaitlistRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Persists WaitlistEntry: Adds a user to the waitlist for an event (CREATE)
     * @param entry    The WaitlistEntry containing userId, eventId, and initial status.
     * @param callback Callback to handle success or failure.
     */
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

    /**
     * Fetches all waitlist entries associated with an event. (READ)
     * @param eventId  The event ID
     * @param callback Callback returning a List of WaitlistEntry objects.
     */
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

    /**
     * Updates the status of a specific user's entry in an event's waitlist. (UPDATE)
     * @param eventId   The event ID.
     * @param userId    The user ID
     * @param status    The new Status (e.g. SELECTED, REJECTED)
     * @param callback  Callback to handle completion.
     */
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

    /**
     * Removes a user from the waitlist of a specific event. (DELETE)
     * @param eventId   The event ID
     * @param userId    The user ID
     * @param callback  Handles completion
     */
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

    /**
     * Selects a random subset of entrants from an event's waitlist based on event capacity;
     * fetches the entire waitlist, shuffles it (Random), and returns the top n users.
     * TODO: make this happen remotely when?
     * @param eventId  The ID of the event to draw from.
     * @param capacity The maximum number of winners to select.
     * @param callback Callback returning a List of the selected WaitlistEntry objects.
     */
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

    /**
     * Get a specific user's WaitlistEntry for an event
     * @param eventId   event ID
     * @param userId    user ID
     * @param callback  returns WaitlistEntry, or null if not found.
     */
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

    /**
     * Get all waitlists joined by a specific user (across all events)
     * @param userId    The user ID
     * @param callback  Returns list of WaitlistEntries
     */
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

    /**
     * Resets everyone in the waitlist for this event back to Status.WAITLISTED.
     * @param eventId    The event ID
     * @param callback  Returns list of WaitlistEntries
     */
    public void resetWaitlist(String eventId, RepositoryCallback<Void> callback) {
        db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        batch.update(doc.getReference(), "status", Status.WAITLISTED);
                    }
                    batch.commit().addOnSuccessListener(v -> {
                        if (callback != null) callback.onSuccess(null);
                    }).addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                    });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Returns document pertaining to a specific waitllst with a real-time snapshot listener
     *
     * @param eventId
     * @param callback
     * @return A listener from firebase
     */
    public com.google.firebase.firestore.ListenerRegistration listenToWaitlist(String eventId, RepositoryCallback<List<WaitlistEntry>> callback) {
        return db.collection(com.example.zephyrevents.repository.Collections.WAITLIST)
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }
                    List<WaitlistEntry> entries = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                            entries.add(doc.toObject(WaitlistEntry.class));
                        }
                    }
                    callback.onSuccess(entries);
                });
    }
}