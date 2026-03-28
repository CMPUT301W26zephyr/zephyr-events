package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.model.EventComment;
import com.example.zephyrevents.model.Status;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.model.WaitlistEntry;
import com.example.zephyrevents.repository.EventCommentRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;
import com.example.zephyrevents.repository.WaitlistRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventDetailViewActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT = "extra_event";
    public static final String EXTRA_INVITED = "extra_invited";

    private Event event;
    private boolean isInvited;

    private String currentUserId;
    private UserController userController;
    private UserRepository userRepository;
    private final EventCommentRepository commentRepository = new EventCommentRepository();

    private TextView statusTag, eventTitle, eventPrice, eventDate, eventLocation;
    private TextView organizerName, eventAbout, totalCapacity, waitlistCapacity, waitlistApplicants, waitlistRegistrationEnds;
    private View eventImageContainer;

    private View attendeeButtonsContainer;

    private Button buttonPrimary, buttonSecondary;

    private NestedScrollView nestedScrollView;
    private View sectionAbout;
    private View sectionWaitlist;
    private View sectionComments;
    private View sectionManage;

    private LinearLayout tabAbout;
    private LinearLayout tabWaitlist;
    private LinearLayout tabComments;
    private LinearLayout tabManage;
    private TextView tabAboutLabel;
    private TextView tabWaitlistLabel;
    private TextView tabCommentsLabel;
    private TextView tabManageLabel;
    private View tabAboutUnderline;
    private View tabWaitlistUnderline;
    private View tabCommentsUnderline;
    private View tabManageUnderline;

    private TextView commentsSectionTitle;
    private TextView addCommentAction;
    private RecyclerView commentsRecycler;

    private EventCommentAdapter commentAdapter;
    private ListenerRegistration commentsRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        userController = new UserController(this);
        userRepository = new UserRepository();

        currentUserId = userController.getCurrentUserId();
        if (currentUserId == null) currentUserId = "unknown_user";

        findViews();
        setupBackButton();
        setupTabsAndScroll();
        setupCommentsUi();
        setupManageActions();

        String eventId = getIntent().getStringExtra(EXTRA_EVENT);
        isInvited = getIntent().getBooleanExtra(EXTRA_INVITED, false);

        if (eventId == null) {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EventController.getInstance().getEventById(eventId, new RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                event = result;
                if (event != null) {
                    populateUI();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        detachCommentsListener();
        super.onDestroy();
    }

    private void findViews() {
        statusTag = findViewById(R.id.status_tag);
        eventTitle = findViewById(R.id.event_title);
        eventPrice = findViewById(R.id.event_price);
        eventDate = findViewById(R.id.event_date);
        eventLocation = findViewById(R.id.event_location);
        organizerName = findViewById(R.id.organizer_name);
        eventAbout = findViewById(R.id.event_about);
        totalCapacity = findViewById(R.id.total_capacity);
        waitlistCapacity = findViewById(R.id.waitlist_capacity);

        waitlistApplicants = findViewById(R.id.waitlist_applicants);
        waitlistRegistrationEnds = findViewById(R.id.waitlist_registration_ends);
        eventImageContainer = findViewById(R.id.event_image_container);

        attendeeButtonsContainer = findViewById(R.id.event_detail_buttons);

        buttonPrimary = findViewById(R.id.button_primary);
        buttonSecondary = findViewById(R.id.button_secondary);

        nestedScrollView = findViewById(R.id.event_detail_scroll);
        sectionAbout = findViewById(R.id.section_about);
        sectionWaitlist = findViewById(R.id.section_waitlist);
        sectionComments = findViewById(R.id.section_comments);
        sectionManage = findViewById(R.id.section_manage);

        tabAbout = findViewById(R.id.tab_about);
        tabWaitlist = findViewById(R.id.tab_waitlist);
        tabComments = findViewById(R.id.tab_comments);
        tabManage = findViewById(R.id.tab_manage);
        tabAboutLabel = findViewById(R.id.tab_about_label);
        tabWaitlistLabel = findViewById(R.id.tab_waitlist_label);
        tabCommentsLabel = findViewById(R.id.tab_comments_label);
        tabManageLabel = findViewById(R.id.tab_manage_label);
        tabAboutUnderline = findViewById(R.id.tab_about_underline);
        tabWaitlistUnderline = findViewById(R.id.tab_waitlist_underline);
        tabCommentsUnderline = findViewById(R.id.tab_comments_underline);
        tabManageUnderline = findViewById(R.id.tab_manage_underline);

        commentsSectionTitle = findViewById(R.id.comments_section_title);
        addCommentAction = findViewById(R.id.add_comment_action);
        commentsRecycler = findViewById(R.id.comments_recycler);
    }

    private void setupCommentsUi() {
        commentAdapter = new EventCommentAdapter(new EventCommentAdapter.CommentRowListener() {
            @Override
            public void onReply(@NonNull EventComment parentComment) {
                openCommentComposer(parentComment.getId(), parentComment);
            }

            @Override
            public void onDeleteRequested(@NonNull EventComment comment) {
                confirmDeleteComment(comment);
            }
        });
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        commentsRecycler.setNestedScrollingEnabled(false);
        commentsRecycler.setAdapter(commentAdapter);

        addCommentAction.setOnClickListener(v -> openCommentComposer(null, null));
    }

    private void confirmDeleteComment(@NonNull EventComment comment) {
        if (comment.getId() == null) return;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_comment_title)
                .setMessage(R.string.delete_comment_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_comment, (d, w) ->
                        commentRepository.deleteCommentAndReplies(comment.getId(), new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(EventDetailViewActivity.this, R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(EventDetailViewActivity.this, R.string.delete_comment_failed, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void setupManageActions() {
        View rowEntrants = findViewById(R.id.manage_row_entrants);
        View rowEdit = findViewById(R.id.manage_row_edit);
        View rowQr = findViewById(R.id.manage_row_qr);
        View rowMap = findViewById(R.id.manage_row_map);

        if (rowEntrants != null) {
            rowEntrants.setOnClickListener(v -> {
                if (event == null) return;
                Intent intent = new Intent(this, OrganizerEntrantsListView.class);
                intent.putExtra(EXTRA_EVENT, event.getEventId());
                startActivity(intent);
            });
        }
        if (rowEdit != null) {
            rowEdit.setOnClickListener(v -> {
                if (event == null) return;
                Intent intent = new Intent(this, OrganizerEventAddEditView.class);
                intent.putExtra("EXTRA_EDIT_EVENT_ID", event.getEventId());
                startActivity(intent);
                finish();
            });
        }
        if (rowQr != null) {
            rowQr.setOnClickListener(v -> Toast.makeText(this, R.string.qr_not_available, Toast.LENGTH_SHORT).show());
        }
        if (rowMap != null) {
            rowMap.setOnClickListener(v -> Toast.makeText(this, R.string.map_not_available, Toast.LENGTH_SHORT).show());
        }
    }

    private void showOrganizerRunLotteryButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonSecondary.setVisibility(View.GONE);
        buttonPrimary.setText(R.string.run_lottery);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            if (event == null) return;
            Intent intent = new Intent(EventDetailViewActivity.this, OrganizerEntrantsListView.class);
            intent.putExtra(EXTRA_EVENT, event.getEventId());
            startActivity(intent);
        });
    }

    private void openCommentComposer(@androidx.annotation.Nullable String parentCommentId, @androidx.annotation.Nullable EventComment replyTo) {
        if ("unknown_user".equals(currentUserId)) {
            Toast.makeText(this, "Sign in to comment.", Toast.LENGTH_SHORT).show();
            return;
        }
        userRepository.getUserById(currentUserId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                String authorName = user != null && user.getName() != null ? user.getName() : "User";
                String letter = authorName.isEmpty() ? "?" : authorName.substring(0, 1).toUpperCase(Locale.getDefault());
                String title = parentCommentId == null
                        ? getString(R.string.new_comment)
                        : getString(R.string.reply);
                String subtitle = replyTo != null && replyTo.getAuthorName() != null
                        ? getString(R.string.reply_to_user, replyTo.getAuthorName())
                        : null;
                CommentComposeBottomSheet.show(EventDetailViewActivity.this, title, subtitle, letter, text -> postComment(text, parentCommentId, authorName));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Could not load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String text, @androidx.annotation.Nullable String parentCommentId, String authorName) {
        if (event == null) return;
        EventComment c = new EventComment();
        c.setEventId(event.getEventId());
        c.setUserId(currentUserId);
        c.setAuthorName(authorName);
        c.setBody(text);
        c.setCreatedAt(System.currentTimeMillis());
        c.setParentCommentId(parentCommentId);

        commentRepository.addComment(c, new RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(EventDetailViewActivity.this, "Posted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Could not post comment.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void detachCommentsListener() {
        if (commentsRegistration != null) {
            commentsRegistration.remove();
            commentsRegistration = null;
        }
    }

    private void attachCommentsListener() {
        detachCommentsListener();
        if (event == null) return;
        boolean isOrganizer = currentUserId != null && currentUserId.equals(event.getOrganizerId());
        commentAdapter.setOrganizerContext(event.getOrganizerId(), isOrganizer);
        commentsRegistration = commentRepository.listenToEventComments(event.getEventId(), new RepositoryCallback<List<EventComment>>() {
            @Override
            public void onSuccess(List<EventComment> result) {
                commentAdapter.submit(result);
                int n = result != null ? result.size() : 0;
                commentsSectionTitle.setText(getString(R.string.comments_header, n));
            }

            @Override
            public void onFailure(Exception e) {
                commentAdapter.submit(null);
                commentsSectionTitle.setText(getString(R.string.comments_header, 0));
            }
        });
    }

    private void setupTabsAndScroll() {
        tabAbout.setOnClickListener(v -> {
            selectTab(0);
            scrollSectionIntoView(sectionAbout);
        });
        tabWaitlist.setOnClickListener(v -> {
            selectTab(1);
            scrollSectionIntoView(sectionWaitlist);
        });
        tabComments.setOnClickListener(v -> {
            selectTab(2);
            scrollSectionIntoView(sectionComments);
        });
        tabManage.setOnClickListener(v -> {
            selectTab(3);
            scrollSectionIntoView(sectionManage);
        });
    }

    private void selectTab(int index) {
        int active = ContextCompat.getColor(this, R.color.primary_red);
        int inactive = ContextCompat.getColor(this, R.color.text_secondary);

        tabAboutUnderline.setVisibility(index == 0 ? View.VISIBLE : View.INVISIBLE);
        tabWaitlistUnderline.setVisibility(index == 1 ? View.VISIBLE : View.INVISIBLE);
        tabCommentsUnderline.setVisibility(index == 2 ? View.VISIBLE : View.INVISIBLE);
        if (tabManage.getVisibility() == View.VISIBLE) {
            tabManageUnderline.setVisibility(index == 3 ? View.VISIBLE : View.INVISIBLE);
        }

        tabAboutLabel.setTextColor(index == 0 ? active : inactive);
        tabWaitlistLabel.setTextColor(index == 1 ? active : inactive);
        tabCommentsLabel.setTextColor(index == 2 ? active : inactive);
        if (tabManage.getVisibility() == View.VISIBLE) {
            tabManageLabel.setTextColor(index == 3 ? active : inactive);
        }

        tabAboutLabel.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabWaitlistLabel.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabCommentsLabel.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        if (tabManage.getVisibility() == View.VISIBLE) {
            tabManageLabel.setTypeface(null, index == 3 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void scrollSectionIntoView(@NonNull View target) {
        nestedScrollView.post(() -> {
            int y = 0;
            View v = target;
            while (v != null && v != nestedScrollView) {
                y += v.getTop();
                if (!(v.getParent() instanceof View)) break;
                v = (View) v.getParent();
            }
            nestedScrollView.smoothScrollTo(0, Math.max(0, y));
        });
    }

    private void setupBackButton() {
        ImageButton back = findViewById(R.id.button_back);
        if (back != null) back.setOnClickListener(v -> finish());
    }

    private void populateUI() {
        if (event == null) return;

        eventTitle.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

        if (event.getTime() != null && event.getTime().getStartTime() > 0) {
            eventDate.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(event.getTime().getStartTime())));
        } else {
            eventDate.setText(getString(R.string.date));
        }

        if (event.getLocation() != null && event.getLocation().getLocationString() != null) {
            eventLocation.setText(event.getLocation().getLocationString());
        } else {
            eventLocation.setText(R.string.location);
        }

        eventAbout.setText(event.getDescription() != null ? event.getDescription() : "No description provided.");

        if (totalCapacity != null) {
            totalCapacity.setText(String.valueOf(event.getCapacity()));
        }

        String limitStr = (event.getWaitlistCapacity() != null && event.getWaitlistCapacity() > 0)
                ? String.valueOf(event.getWaitlistCapacity())
                : "Unlimited";
        if (waitlistCapacity != null) {
            waitlistCapacity.setText(limitStr);
        }

        if (event.getRegistrationEndTime() > 0) {
            waitlistRegistrationEnds.setText(new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(event.getRegistrationEndTime())));
        } else {
            waitlistRegistrationEnds.setText("N/A");
        }

        eventImageContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.event_placeholder_swimming));

        boolean isOrganizer = currentUserId != null && currentUserId.equals(event.getOrganizerId());

        MaterialCardView organizerCard = findViewById(R.id.organizer_card);
        if (organizerCard != null) {
            organizerCard.setOnClickListener(v -> { /* reserved: organizer profile */ });
        }

        if (isOrganizer) {
            organizerName.setText("You");
            tabManage.setVisibility(View.VISIBLE);
            sectionManage.setVisibility(View.VISIBLE);
            attendeeButtonsContainer.setVisibility(View.VISIBLE);
            showOrganizerRunLotteryButton();
            addCommentAction.setVisibility(View.VISIBLE);
        } else {
            tabManage.setVisibility(View.GONE);
            sectionManage.setVisibility(View.GONE);
            attendeeButtonsContainer.setVisibility(View.VISIBLE);
            addCommentAction.setVisibility(View.VISIBLE);

            if (event.getOrganizerId() != null) {
                userRepository.getUserById(event.getOrganizerId(), new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        organizerName.setText(user != null && user.getName() != null ? user.getName() : "Unknown Organizer");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        organizerName.setText("Unknown Organizer");
                    }
                });
            } else {
                organizerName.setText("Unknown Organizer");
            }
        }

        attachCommentsListener();

        if (isInvited) {
            nestedScrollView.post(() -> {
                selectTab(1);
                scrollSectionIntoView(sectionWaitlist);
            });
        }

        new WaitlistRepository().getWaitlist(event.getEventId(), new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onSuccess(List<WaitlistEntry> entries) {
                int trueCount = (entries != null) ? entries.size() : 0;
                waitlistApplicants.setText(String.valueOf(trueCount));

                boolean lotteryRun = false;
                WaitlistEntry myEntry = null;

                if (entries != null) {
                    for (WaitlistEntry e : entries) {
                        if (e.getStatus() != Status.WAITLISTED) {
                            lotteryRun = true;
                        }
                        if (e.getUserId() != null && e.getUserId().equals(currentUserId)) {
                            myEntry = e;
                        }
                    }
                }

                boolean trueCapacityFull = event.getWaitlistCapacity() != null && event.getWaitlistCapacity() > 0 && trueCount >= event.getWaitlistCapacity();
                boolean pastDeadline = event.getRegistrationEndTime() > 0 && System.currentTimeMillis() > event.getRegistrationEndTime();

                boolean isClosedForNew = trueCapacityFull || lotteryRun || pastDeadline;

                if (isClosedForNew) {
                    statusTag.setText("CLOSED");
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag_orange));
                } else {
                    statusTag.setText(R.string.registration_open);
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag));
                }

                if (!isOrganizer) {
                    if (myEntry == null) {
                        if (isClosedForNew) {
                            showWaitlistClosedButton(trueCapacityFull, lotteryRun, pastDeadline);
                        } else {
                            showJoinWaitlistButton(new WaitlistRepository());
                        }
                    } else {
                        switch (myEntry.getStatus()) {
                            case WAITLISTED:
                                showLeaveWaitlistButton(new WaitlistRepository());
                                break;
                            case SELECTED:
                                showAcceptDeclineButtons(new WaitlistRepository());
                                break;
                            case LOST:
                                showLostButton();
                                break;
                            case ACCEPTED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: CONFIRMED");
                                buttonPrimary.setBackgroundColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.youre_in_green));
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                            case DECLINED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: DECLINED");
                                buttonPrimary.setBackgroundColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.invite_declined_red));
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                waitlistApplicants.setText(String.valueOf(event.getCurrentApplicants()));
            }
        });
    }

    private void showJoinWaitlistButton(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.join_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            WaitlistEntry newEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0.0, 0.0, Status.WAITLISTED);
            repo.addUserToWaitlist(newEntry, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                    populateUI();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLeaveWaitlistButton(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.leave_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_outline));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
        buttonPrimary.setOnClickListener(v -> {
            repo.removeUserFromWaitlist(event.getEventId(), currentUserId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "Left Waitlist", Toast.LENGTH_SHORT).show();
                    populateUI();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        });
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showAcceptDeclineButtons(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.accept_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            repo.updateStatus(event.getEventId(), currentUserId, Status.ACCEPTED, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(EventDetailViewActivity.this, EventStatusActivity.class);
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
                    intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_ACCEPTED);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        });

        buttonSecondary.setVisibility(View.VISIBLE);
        buttonSecondary.setText(R.string.decline_invite);
        buttonSecondary.setOnClickListener(v -> {
            repo.updateStatus(event.getEventId(), currentUserId, Status.DECLINED, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Intent intent = new Intent(EventDetailViewActivity.this, EventStatusActivity.class);
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
                    intent.putExtra(EventStatusActivity.EXTRA_EVENT_KEY, event.getEventId());
                    intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, EventStatusActivity.STATUS_DECLINED);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        });
    }

    private void showWaitlistClosedButton(boolean capacity, boolean lottery, boolean deadline) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);

        if (lottery) {
            buttonPrimary.setText("LOTTERY COMPLETE");
        } else if (deadline) {
            buttonPrimary.setText("REGISTRATION CLOSED");
        } else {
            buttonPrimary.setText(R.string.capacity_full);
        }

        buttonPrimary.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLostButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText("STATUS: NOT SELECTED");
        buttonPrimary.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }
}
