package com.example.zephyrevents.repository;


//callback.onSuccess(null) for success
//callback.onSuccess(e) for fail
public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);


}
