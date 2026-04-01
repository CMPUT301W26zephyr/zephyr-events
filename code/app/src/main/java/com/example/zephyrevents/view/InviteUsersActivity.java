package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.model.NotificationType;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Search users and invite them as co-organizers or to a private waitlist.
 */
public class InviteUsersActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_CO_ORG = "co_org";
    public static final String MODE_PRIVATE_WAITLIST = "private_waitlist";

    private String eventId;
    private String mode;
    private Event event;
    private String currentUserId;
    private final UserRepository userRepository = new UserRepository();
    private final List<User> results = new ArrayList<>();
    private ResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_users);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (eventId == null || mode == null) {
            finish();
            return;
        }

        currentUserId = new UserController(this).getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = "";
        }

        TextView title = findViewById(R.id.invite_toolbar_title);
        if (MODE_CO_ORG.equals(mode)) {
            title.setText(R.string.invite_coorganizer_title);
        } else {
            title.setText(R.string.invite_entrants_title);
        }

        findViewById(R.id.invite_back).setOnClickListener(v -> finish());

        TextInputEditText searchInput = findViewById(R.id.invite_search_input);
        findViewById(R.id.invite_search_button).setOnClickListener(v -> {
            String q = searchInput.getText() != null ? searchInput.getText().toString() : "";
            runSearch(q);
        });

        RecyclerView rv = findViewById(R.id.invite_results_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultsAdapter();
        rv.setAdapter(adapter);

        EventController.getInstance().getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteUsersActivity.this, "Failed to load event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runSearch(String query) {
        userRepository.searchUsers(query, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> list) {
                results.clear();
                if (list != null) {
                    for (User u : list) {
                        if (u.getId() == null || u.getId().equals(currentUserId)) {
                            continue;
                        }
                        if (event != null && u.getId().equals(event.getOrganizerId())) {
                            continue;
                        }
                        results.add(u);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteUsersActivity.this, "Search failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void persistEvent(Runnable onSuccess) {
        if (event == null) return;
        EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (onSuccess != null) onSuccess.run();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteUsersActivity.this, "Could not save event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(User target, NotificationType type, String body) {
        Notification n = new Notification(target.getId(), eventId, type, body, true, false);
        n.setNotificationId(UUID.randomUUID().toString());
        n.setTime(System.currentTimeMillis());
        new NotificationRepository().saveNotification(n, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(InviteUsersActivity.this, "Notification sent.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteUsersActivity.this, "Could not send notification.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_invite_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            User u = results.get(position);
            h.name.setText(u.getName() != null ? u.getName() : "?");
            String detail = "";
            if (u.getContactInfo() != null) {
                if (u.getContactInfo().getEmail() != null) {
                    detail = u.getContactInfo().getEmail();
                } else if (u.getContactInfo().getPhone() != null) {
                    detail = u.getContactInfo().getPhone();
                }
            }
            h.detail.setText(detail);

            if (event == null) {
                h.action.setEnabled(false);
                return;
            }
            h.action.setEnabled(true);

            boolean isCo = event.getCoOrganizerUserIds().contains(u.getId());
            boolean isPendingCo = event.getPendingCoOrganizerUserIds().contains(u.getId());
            boolean isPendingWaitlist = event.getPendingPrivateWaitlistInviteUserIds().contains(u.getId());

            if (MODE_CO_ORG.equals(mode)) {
                if (isCo || isPendingCo) {
                    h.action.setText(R.string.remove_action);
                    h.action.setOnClickListener(v -> {
                        event.getCoOrganizerUserIds().remove(u.getId());
                        event.getPendingCoOrganizerUserIds().remove(u.getId());
                        persistEvent(null);
                    });
                } else {
                    h.action.setText(R.string.invite_action);
                    h.action.setOnClickListener(v -> {
                        if (!event.getPendingCoOrganizerUserIds().contains(u.getId())) {
                            event.getPendingCoOrganizerUserIds().add(u.getId());
                        }
                        String body = getString(R.string.notif_coorganizer_body,
                                event.getName() != null ? event.getName() : "event");
                        persistEvent(() -> sendNotification(u, NotificationType.CO_ORGANIZER_INVITE, body));
                    });
                }
            } else {
                if (isPendingWaitlist) {
                    h.action.setText(R.string.remove_action);
                    h.action.setOnClickListener(v -> {
                        event.getPendingPrivateWaitlistInviteUserIds().remove(u.getId());
                        persistEvent(null);
                    });
                } else {
                    h.action.setText(R.string.invite_action);
                    h.action.setOnClickListener(v -> {
                        if (event.getCoOrganizerUserIds().contains(u.getId())) {
                            Toast.makeText(InviteUsersActivity.this, R.string.coorganizer_cannot_join, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!event.getPendingPrivateWaitlistInviteUserIds().contains(u.getId())) {
                            event.getPendingPrivateWaitlistInviteUserIds().add(u.getId());
                        }
                        String body = getString(R.string.notif_private_invite_body,
                                event.getName() != null ? event.getName() : "event");
                        persistEvent(() -> sendNotification(u, NotificationType.PRIVATE_EVENT_INVITE, body));
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView detail;
            final MaterialButton action;

            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.user_invite_name);
                detail = itemView.findViewById(R.id.user_invite_detail);
                action = itemView.findViewById(R.id.user_invite_action);
            }
        }
    }
}
