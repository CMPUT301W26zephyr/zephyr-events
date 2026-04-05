package com.example.zephyrevents.controller;

import androidx.annotation.VisibleForTesting;

import com.example.zephyrevents.model.SystemLog;
import com.example.zephyrevents.repository.SystemLogRepository;

public class SystemLogController {
    private static SystemLogController instance;
    private final SystemLogRepository repository;

    private SystemLogController() {
        this.repository = new SystemLogRepository();
    }

    @VisibleForTesting
    SystemLogController(SystemLogRepository repository) {
        this.repository = repository;
    }

    public static SystemLogController getInstance() {
        if (instance == null) instance = new SystemLogController();
        return instance;
    }

    public void logAction(String actionType, String description, String actorName) {
        SystemLog log = new SystemLog(actionType, description, actorName);
        repository.addLog(log);
    }
}