package com.example.zephyrevents.repository;

import com.example.zephyrevents.model.SystemLog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

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
    public void addLog(SystemLog log) {
        db.collection("system_logs").document(log.getId()).set(log);
    }

    public void getAllLogs(RepositoryCallback<List<SystemLog>> callback) {
        db.collection("system_logs")
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