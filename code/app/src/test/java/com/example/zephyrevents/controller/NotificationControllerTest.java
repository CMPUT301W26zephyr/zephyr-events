package com.example.zephyrevents.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.model.NotifyingGroup;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotificationControllerTest {
    private static final class Harness implements AutoCloseable {
        private final MockedConstruction<NotificationRepository> construction;
        final NotificationController controller;
        final NotificationRepository repo;

        Harness() {
            construction = Mockito.mockConstruction(NotificationRepository.class);
            controller = new NotificationController();
            repo = construction.constructed().get(0);
        }

        @Override
        public void close() {
            construction.close();
        }
    }

        @Test
        // Send the automatic system notification based on an event
        public void sendAutomaticNotification() {
            try (Harness h = new Harness()) {
                h.controller.sendAutomaticNotification(
                        "user-1",
                        "event-42",
                        NotificationType.LOTTERY_COMPLETED,
                        "Lottery finished");
                ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
                verify(h.repo).saveNotification(captor.capture(), any(RepositoryCallback.class));
                Notification n = captor.getValue();
                assertEquals("user-1", n.getUserId());
                assertEquals("event-42", n.getEventId());
                assertEquals(NotificationType.LOTTERY_COMPLETED, n.getType());
                assertEquals("Lottery finished", n.getText());
            }
        }

    @Test
    // Test that sendAutomaticNotifcation only triggers one repository save per call
    public void sendAutomaticNotification_callsSaveOnce() {
        try (Harness h = new Harness()) {
            h.controller.sendAutomaticNotification("u", "e", NotificationType.MANUAL, "hi");
            verify(h.repo, times(1))
                    .saveNotification(any(Notification.class), any(RepositoryCallback.class));
        }
    }
    @Test
    //
    public void notifyUsers_emptyList_doesNotSave(){
        try (Harness h = new Harness()){
            h.controller.notifyUsers(Collections.emptyList(), "event-1", NotifyingGroup.WAITLIST);
            verify(h.repo, never()).saveNotification(any(), any());


        }
    }

    @Test
    public void notifyUsers_winners(){
        try (Harness h = new Harness()) {
            h.controller.notifyUsers(Arrays.asList("a", "b"), "evt-9", NotifyingGroup.WINNERS);
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(h.repo, times(2))
                    .saveNotification(captor.capture(), any(RepositoryCallback.class));
            List<Notification> saved = captor.getAllValues();
            assertEquals("Congratulations! You have been selected for this event.", saved.get(0).getText());
            assertEquals("Congratulations! You have been selected for this event.", saved.get(1).getText());
            assertEquals(NotificationType.MANUAL, saved.get(0).getType());
            assertEquals("a", saved.get(0).getUserId());
            assertEquals("b", saved.get(1).getUserId());
        }

    }

    @Test
    public void notifyUsersWithCustomMessage_empty_doesNotSave(){
        try (Harness h = new Harness()) {
            h.controller.notifyUsersWithCustomMessage(Collections.emptyList(), "e","msg");
            verify(h.repo, never()).saveNotification(any(), any());

        }

    }

    @Test
    public void notifyUsersWithCustomMessage_savesWithCustomText() {
        try (Harness h = new Harness()) {
            h.controller.notifyUsersWithCustomMessage(
                    Collections.singletonList("u99"), "e5", "Custom body");
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(h.repo).saveNotification(captor.capture(), any(RepositoryCallback.class));
            Notification n = captor.getValue();
            assertEquals("Custom body", n.getText());
            assertEquals(NotificationType.MANUAL, n.getType());
            assertEquals("u99", n.getUserId());
            assertEquals("e5", n.getEventId());
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void getUserNotifications_forwardsCallbackFromRepository() {
        try (Harness h = new Harness()) {
            RepositoryCallback<List<Notification>> outer =
                    Mockito.mock(RepositoryCallback.class);
            h.controller.getUserNotifications("uid-7", outer);
            ArgumentCaptor<RepositoryCallback<List<Notification>>> innerCaptor =
                    ArgumentCaptor.forClass(RepositoryCallback.class);
            verify(h.repo).getUserNotifications(eq("uid-7"), innerCaptor.capture());
            List<Notification> payload = Collections.emptyList();
            innerCaptor.getValue().onSuccess(payload);
            verify(outer).onSuccess(payload);
        }
    }




}
