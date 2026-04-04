package com.example.zephyrevents.controller;

import com.example.zephyrevents.model.SystemLog;
import com.example.zephyrevents.repository.SystemLogRepository;

public class SystemLogController {
    private static SystemLogController instance;
    private final SystemLogRepository repository;

    private SystemLogController() {
        repository = new SystemLogRepository();
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