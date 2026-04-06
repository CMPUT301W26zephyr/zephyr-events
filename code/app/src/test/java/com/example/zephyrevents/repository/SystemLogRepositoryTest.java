package com.example.zephyrevents.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.example.zephyrevents.model.SystemLog;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

// Anthropic, Claude, "Write Mockito unit tests for SystemLogRepository", 2026-04-05

public class SystemLogRepositoryTest {

    private SystemLogRepository repository;
    private RepositoryCallback<List<SystemLog>> mockListCallback;

    private FirebaseFirestore mockDb;
    private CollectionReference mockCollection;
    private DocumentReference mockDocument;
    private Query mockQuery;
    private Task mockTask;

    @Before
    public void setup() {
        mockListCallback = mock(RepositoryCallback.class);

        mockDb         = mock(FirebaseFirestore.class);
        mockCollection = mock(CollectionReference.class);
        mockDocument   = mock(DocumentReference.class);
        mockQuery      = mock(Query.class);
        mockTask       = mock(Task.class);

        // stub the chain: db.collection(...) -> CollectionReference
        when(mockDb.collection(anyString())).thenReturn(mockCollection);

        // stub for addLog: collection.document(...) -> DocumentReference
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any())).thenReturn(mockTask);

        // stub for getAllLogs: collection.orderBy(...) -> Query -> Task
        when(mockCollection.orderBy(anyString(), any(Query.Direction.class))).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        repository = new SystemLogRepository(mockDb);
    }

    @Test
    public void addLog_validLog_doesNotThrow() {
        SystemLog log = new SystemLog();
        log.setId("log-001");
        repository.addLog(log);
    }

    @Test
    public void getAllLogs_validCallback_neverCallsOnFailureImmediately() {
        repository.getAllLogs(mockListCallback);
        verify(mockListCallback, never()).onFailure(any());
    }

    @Test
    public void getAllLogs_validCallback_neverCallsOnSuccessImmediately() {
        repository.getAllLogs(mockListCallback);
        verify(mockListCallback, never()).onSuccess(any());
    }
}