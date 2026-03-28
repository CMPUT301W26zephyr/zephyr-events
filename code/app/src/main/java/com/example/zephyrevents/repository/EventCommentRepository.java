package com.example.zephyrevents.repository;

import androidx.annotation.NonNull;

import com.example.zephyrevents.model.EventComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Firestore persistence for event comments. Listeners receive updates when data changes remotely.
 */
public class EventCommentRepository {

    private final FirebaseFirestore db;

    public EventCommentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public EventCommentRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Real-time stream of all comments for an event (top-level and replies).
     */
    public ListenerRegistration listenToEventComments(
            @NonNull String eventId,
            @NonNull RepositoryCallback<List<EventComment>> callback) {

        return db.collection(Collections.EVENT_COMMENTS)
                .whereEqualTo("eventId", eventId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onFailure(error);
                        return;
                    }
                    if (snapshot == null) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<EventComment> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        EventComment c = doc.toObject(EventComment.class);
                        if (c != null) {
                            c.setId(doc.getId());
                            list.add(c);
                        }
                    }
                    list.sort(Comparator.comparingLong(EventComment::getCreatedAt));
                    callback.onSuccess(list);
                });
    }

    /**
     * Saves a new comment or reply. The {@link EventComment#getId()} may be null; Firestore assigns it.
     */
    public void addComment(@NonNull EventComment comment, @NonNull RepositoryCallback<String> callback) {
        if (comment.getEventId() == null || comment.getUserId() == null) {
            callback.onFailure(new IllegalArgumentException("eventId and userId are required"));
            return;
        }
        comment.setId(null);
        db.collection(Collections.EVENT_COMMENTS)
                .add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Deletes a comment document and any direct replies ({@code parentCommentId} == {@code commentId}).
     */
    public void deleteCommentAndReplies(@NonNull String commentId, @NonNull RepositoryCallback<Void> callback) {
        db.collection(Collections.EVENT_COMMENTS)
                .whereEqualTo("parentCommentId", commentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection(Collections.EVENT_COMMENTS).document(commentId));
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
