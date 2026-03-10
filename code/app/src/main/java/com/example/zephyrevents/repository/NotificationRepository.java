// file name: NotificationRepository
// for save, track notification data

package com.example.zephyrevents.repository;
import com.example.zephyrevents.model.Notification;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;

// might use notificationid userid eventid type timestamp etc.


public class NotificationRepository {
    private final FirebaseFirestore db;
    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    //event create -> user get notification
    public void createNotification(Notification notification,
                                   RepositoryCallback<Void> callback) {
        //TODO: FIREBASE CODE
    }

    public void getUserNotifications(String userId,
                                     // see if save was successful/fail
                                     RepositoryCallback<List<Notification>> callback) {
        //TODO: FIREBASE CODE
    }
}