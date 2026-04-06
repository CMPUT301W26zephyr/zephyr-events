package com.example.zephyrevents.repository;

import com.example.zephyrevents.model.SystemLog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists and loads {@link SystemLog} records from Firestore (collection {@code system_logs}).
 */
public class SystemLogRepository {

    private final FirebaseFirestore db;

    /** Default constructor. Uses the production Firestore instance. */
    public SystemLogRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Constructor with dependency injection for testing. */
    public SystemLogRepository(FirebaseFirestore db) {
        this.db = db;
    }


    /**
     * Saves a log entry to Firestore using the log id as the document id
     * @param log entry to store
     */
    public void addLog(SystemLog log) {
        db.collection(Collections.SYSTEM_LOGS).document(log.getId()).set(log);
    }

    /**
     * Load all the logs, with the newest first and returns them on success.
     * @param callback receives the list or an error
     */
    public void getAllLogs(RepositoryCallback<List<SystemLog>> callback) {
        db.collection(Collections.SYSTEM_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SystemLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        logs.add(doc.toObject(SystemLog.class));
                    }
                    callback.onSuccess(logs);
                })
                .addOnFailureListener(callback::onFailure);
    }
}