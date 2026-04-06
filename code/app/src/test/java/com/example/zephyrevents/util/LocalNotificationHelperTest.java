package com.example.zephyrevents.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalNotificationHelperTest {

    @Mock
    Context context;
    @Mock
    NotificationManager notificationManager;

    @Before
    public void setUp() {
        when(context.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(notificationManager);
    }

    private MockedConstruction<NotificationCompat.Builder> mockNotificationCompatBuilder() {
        return Mockito.mockConstruction(NotificationCompat.Builder.class, (mock, ctx) -> {
            when(mock.setSmallIcon(anyInt())).thenReturn(mock);
            when(mock.setContentTitle(any())).thenReturn(mock);
            when(mock.setContentText(any())).thenReturn(mock);
            when(mock.setAutoCancel(anyBoolean())).thenReturn(mock);
            when(mock.setPriority(anyInt())).thenReturn(mock);
            when(mock.setContentIntent(any())).thenReturn(mock);
            when(mock.build()).thenReturn(mock(Notification.class));
        });
    }

    /** Return a fake {@link PendingIntent} for any static call (covers all {@code getActivity} overloads). */
    private static MockedStatic<PendingIntent> mockAllPendingIntentStatics() {
        return Mockito.mockStatic(
                PendingIntent.class,
                Mockito.withSettings().defaultAnswer(invocation -> mock(PendingIntent.class)));
    }

    @Test
    public void showNotification_postsWithSameNotificationId_whenEventIdProvided() {
        try (MockedConstruction<NotificationCompat.Builder> ignored = mockNotificationCompatBuilder();
             MockedStatic<PendingIntent> ignoredPi = mockAllPendingIntentStatics()) {
            LocalNotificationHelper.showNotification(context, "Title", "Body", 42, "evt-123");
            verify(notificationManager).notify(eq(42), any(Notification.class));
        }
    }

    @Test
    public void showNotification_postsWithSameNotificationId_whenEventIdNull() {
        try (MockedConstruction<NotificationCompat.Builder> ignored = mockNotificationCompatBuilder();
             MockedStatic<PendingIntent> ignoredPi = mockAllPendingIntentStatics()) {
            LocalNotificationHelper.showNotification(context, "T", "M", 7, null);
            verify(notificationManager).notify(eq(7), any(Notification.class));
        }
    }

    @Test
    public void showNotification_postsWithSameNotificationId_whenEventIdEmpty() {
        try (MockedConstruction<NotificationCompat.Builder> ignored = mockNotificationCompatBuilder();
             MockedStatic<PendingIntent> ignoredPi = mockAllPendingIntentStatics()) {
            LocalNotificationHelper.showNotification(context, "T", "M", 99, "");
            verify(notificationManager).notify(eq(99), any(Notification.class));
        }
    }
}
