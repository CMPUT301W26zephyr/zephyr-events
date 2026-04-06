package com.example.zephyrevents.controller;

import androidx.annotation.VisibleForTesting;

import com.example.zephyrevents.model.SystemLog;
import com.example.zephyrevents.repository.SystemLogRepository;

/**
 * Singleton controller that records system or admin actions by saving the logged entries
 */
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

    /**
     * Returns the shared controller instance.
     */

    public static SystemLogController getInstance() {
        if (instance == null) instance = new SystemLogController();
        return instance;
    }
    /**
     * Builds a log for the given action and stores it in the repository.
     */
    public void logAction(String actionType, String description, String actorName) {
        SystemLog log = new SystemLog(actionType, description, actorName);
        repository.addLog(log);
    }
}