package com.example.zephyrevents.repository;


//callback.onSuccess(null) for success
//callback.onSuccess(e) for fail

/**
 * Generic callback for repository operations.
 * @param <T>   The result data
 */
public interface RepositoryCallback<T> {
    /**
     * Called when repository operation completes successfully.
     * @param result    The result of the operation, or null if no return value.
     */
    void onSuccess(T result);

    /**
     * Called when repository operation fails.
     * @param e The exception that caused the failure.
     */
    void onFailure(Exception e);

}
