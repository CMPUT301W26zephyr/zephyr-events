package com.example.zephyrevents.view;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.zephyrevents.util.DialogUiHelper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.SystemLogController;
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
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class EventDetailViewActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT = "extra_event";
    public static final String EXTRA_INVITED = "extra_invited";

    private Event event;
    private boolean isInvited;

    private String currentUserId;
    private UserController userController;
    private UserRepository userRepository;
    private final EventCommentRepository commentRepository = new EventCommentRepository();
    private WaitlistRepository pendingRepo; // store repo while waiting for permission

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
    /** True when organizer/co-organizer/admin may delete any comment (also used to authorize delete). */
    private boolean commentCanModerate;
    private ListenerRegistration eventRegistration;
    private ListenerRegistration eventResumeRegistration;

    private ListenerRegistration waitlistRegistration;


    private ImageView eventPoster;
    private TextView inviteContextText;

    private FrameLayout waitlistCenterOverlay;
    private MaterialCardView waitlistOverlayCard;
    private ImageView waitlistOverlayIcon;
    private TextView waitlistOverlayTitle;
    private TextView waitlistOverlaySubtitle;
    private final Handler overlayHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingWaitlistOverlayHide;

    private ImageView organizerAvatar;

    private boolean isAdminView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        String eventId = getIntent().getStringExtra(EXTRA_EVENT);
        isInvited = getIntent().getBooleanExtra(EXTRA_INVITED, false);
        isAdminView = getIntent().getBooleanExtra("isAdminView", false);
        String eventById = getIntent().getStringExtra(EXTRA_EVENT);

        // Handle link parameter (e.g. from qr code)
        if (eventId == null) {
            Uri data = getIntent().getData();
            if (data != null) {
                eventId = data.getQueryParameter("id");
            }
        }

        if (eventId == null) {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventRegistration = EventController.getInstance().listenToEventById(eventId, new RepositoryCallback<Event>() {
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
        overlayHandler.removeCallbacksAndMessages(null);
        if (eventRegistration != null) eventRegistration.remove();
        if (waitlistRegistration != null) waitlistRegistration.remove();
        if(eventResumeRegistration != null){
            eventResumeRegistration.remove();
            eventResumeRegistration = null;
        }
        super.onDestroy();
    }

    // For refreshing upon returning to the page (e.g. from editing)
    @Override
    protected void onResume() {
        super.onResume();
        String eventId = getIntent().getStringExtra(EXTRA_EVENT);
        if (eventId != null) {
            if(eventResumeRegistration != null){
                eventResumeRegistration.remove();
                eventResumeRegistration = null;
            }
            eventResumeRegistration = EventController.getInstance().listenToEventById(eventId, new RepositoryCallback<Event>() {
                @Override
                public void onSuccess(Event result) {
                    event = result;
                    populateUI();
                }
                @Override public void onFailure(Exception e) {}
            });
        }
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
        eventPoster = findViewById(R.id.event_image);

        attendeeButtonsContainer = findViewById(R.id.bottom_action_container);

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

        inviteContextText = findViewById(R.id.invite_context_text);

        waitlistCenterOverlay = findViewById(R.id.waitlist_center_overlay);
        waitlistOverlayCard = findViewById(R.id.waitlist_overlay_card);
        waitlistOverlayIcon = findViewById(R.id.waitlist_overlay_icon);
        waitlistOverlayTitle = findViewById(R.id.waitlist_overlay_title);
        waitlistOverlaySubtitle = findViewById(R.id.waitlist_overlay_subtitle);
        if (waitlistCenterOverlay != null) {
            waitlistCenterOverlay.setOnClickListener(v -> dismissCenteredWaitlistOverlay());
        }
        if (waitlistOverlayCard != null) {
            waitlistOverlayCard.setOnClickListener(v -> { /* keep tap on card from dismissing */ });
        }
    }

    @NonNull
    private String eventNameForWaitlistStatus() {
        if (event != null && event.getName() != null && !event.getName().trim().isEmpty()) {
            return event.getName().trim();
        }
        return getString(R.string.event_name_placeholder);
    }

    /**
     * Centered card + dimmed scrim over the whole screen (not tied to waitlist section scroll).
     * Tap outside or auto-dismiss after a short delay.
     */
    private void showCenteredWaitlistOverlay(@NonNull String title, @Nullable String subtitle, boolean successStyle) {
        if (waitlistCenterOverlay == null || waitlistOverlayTitle == null) return;
        if (pendingWaitlistOverlayHide != null) {
            overlayHandler.removeCallbacks(pendingWaitlistOverlayHide);
        }
        waitlistCenterOverlay.animate().cancel();
        if (waitlistOverlayCard != null) waitlistOverlayCard.animate().cancel();

        waitlistOverlayTitle.setText(title);
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            waitlistOverlaySubtitle.setText(subtitle);
            waitlistOverlaySubtitle.setVisibility(View.VISIBLE);
        } else {
            waitlistOverlaySubtitle.setVisibility(View.GONE);
        }
        if (waitlistOverlayIcon != null) {
            if (successStyle) {
                waitlistOverlayIcon.setImageResource(R.drawable.ic_check_circle);
                waitlistOverlayIcon.setColorFilter(ContextCompat.getColor(this, R.color.youre_in_green));
            } else {
                waitlistOverlayIcon.setImageResource(R.drawable.ic_cancel_circle);
                waitlistOverlayIcon.setColorFilter(ContextCompat.getColor(this, R.color.invite_declined_red));
            }
        }

        waitlistCenterOverlay.setVisibility(View.VISIBLE);
        waitlistCenterOverlay.setAlpha(0f);
        if (waitlistOverlayCard != null) {
            waitlistOverlayCard.setAlpha(0f);
            waitlistOverlayCard.setScaleX(0.92f);
            waitlistOverlayCard.setScaleY(0.92f);
        }
        waitlistCenterOverlay.animate()
                .alpha(1f)
                .setDuration(220)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        if (waitlistOverlayCard != null) {
            waitlistOverlayCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        pendingWaitlistOverlayHide = this::dismissCenteredWaitlistOverlay;
        overlayHandler.postDelayed(pendingWaitlistOverlayHide, 2600);
    }

    private void dismissCenteredWaitlistOverlay() {
        if (waitlistCenterOverlay == null || waitlistCenterOverlay.getVisibility() != View.VISIBLE) return;
        if (pendingWaitlistOverlayHide != null) {
            overlayHandler.removeCallbacks(pendingWaitlistOverlayHide);
            pendingWaitlistOverlayHide = null;
        }
        waitlistCenterOverlay.animate().cancel();
        if (waitlistOverlayCard != null) waitlistOverlayCard.animate().cancel();
        waitlistCenterOverlay.animate()
                .alpha(0f)
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    waitlistCenterOverlay.setVisibility(View.GONE);
                    waitlistCenterOverlay.setAlpha(1f);
                    if (waitlistOverlayCard != null) {
                        waitlistOverlayCard.setAlpha(1f);
                        waitlistOverlayCard.setScaleX(1f);
                        waitlistOverlayCard.setScaleY(1f);
                    }
                })
                .start();
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
        if (!mayDeleteComment(comment)) {
            Toast.makeText(this, R.string.delete_comment_forbidden, Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog delCommentDialog = new MaterialAlertDialogBuilder(this)
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
                .create();
        delCommentDialog.show();
        DialogUiHelper.applyCompactMaterialActions(delCommentDialog);
    }

    private boolean mayDeleteComment(@NonNull EventComment comment) {
        if (commentCanModerate) return true;
        String authorId = comment.getUserId();
        return authorId != null && authorId.equals(currentUserId);
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
            });
        }
        if (rowQr != null) {
            rowQr.setOnClickListener(v -> {
                if (event == null) return;
                EventQrCodeFragment qrFragment = EventQrCodeFragment.newInstance(event.getEventId());
                qrFragment.show(getSupportFragmentManager(), "EventQrCodeFragment");
            });
        }
        if (rowMap != null) {
            rowMap.setOnClickListener(v -> {
                if (event == null) return;
                Intent intent = new Intent(this, EntrantsMapActivity.class);
                intent.putExtra(EXTRA_EVENT, event.getEventId());
                startActivity(intent);
            });
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
                String avatarUrl = "";
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty()) {
                    avatarUrl = user.getAvatarUrl().trim();
                }
                String avatarForPost = avatarUrl;
                CommentComposeBottomSheet.show(EventDetailViewActivity.this, title, subtitle, letter,
                        avatarForPost.isEmpty() ? null : avatarForPost,
                        text -> postComment(text, parentCommentId, authorName, avatarForPost.isEmpty() ? null : avatarForPost));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Could not load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String text, @androidx.annotation.Nullable String parentCommentId, String authorName,
                               @androidx.annotation.Nullable String authorAvatarUrl) {
        if (event == null) return;
        EventComment c = new EventComment();
        c.setEventId(event.getEventId());
        c.setUserId(currentUserId);
        c.setAuthorName(authorName);
        if (authorAvatarUrl != null && !authorAvatarUrl.trim().isEmpty()) {
            c.setAuthorAvatarUrl(authorAvatarUrl.trim());
        }
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

    // ADDED: admin image delete (replace only image)
    private void showAdminImageDialog() {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.admin_delete_yesorno, null);
        DialogUiHelper.bindAdminDeleteContent(dialogView,
                R.string.admin_delete_title_image,
                R.string.admin_delete_message_image);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.button_cancel)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    finish(); // go back to image list
                });

        dialogView.findViewById(R.id.button_delete)
                .setOnClickListener(v -> {
                    dialog.dismiss();

                    if (event == null) return;

                    EventController.getInstance().saveEventWithOptionalImage(
                            event,
                            null,
                            "",
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    com.example.zephyrevents.controller.SystemLogController.getInstance()
                                            .logAction("ADMIN_DELETED_IMAGE", "Admin deleted poster for event: '" + event.getName() + "'", "Admin");

                                    runOnUiThread(() -> {
                                        Toast.makeText(
                                                EventDetailViewActivity.this,
                                                "Image removed",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        finish(); // refresh UI
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() ->
                                            Toast.makeText(
                                                    EventDetailViewActivity.this,
                                                    "Update failed",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                                }
                            }
                    );
                });

        dialog.show();
    }

    private void showAdminDeleteEventDialog() {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.admin_delete_yesorno, null);
        DialogUiHelper.bindAdminDeleteContent(dialogView,
                R.string.admin_delete_title_event,
                R.string.admin_delete_message_event);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.button_cancel)
                .setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.button_delete)
                .setOnClickListener(v -> {

                    dialog.dismiss();

                    if (event == null) return;

                    EventController.getInstance().deleteEvent(
                            event.getEventId(),
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    SystemLogController.getInstance()
                                            .logAction("ADMIN_DELETED_EVENT", "Admin deleted event: '" + event.getName() + "'", "Admin");
                                    runOnUiThread(() -> {
                                        Toast.makeText(EventDetailViewActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    runOnUiThread(() ->
                                            Toast.makeText(EventDetailViewActivity.this, "Delete failed", Toast.LENGTH_SHORT).show()
                                    );
                                }
                            }
                    );
                });

        dialog.show();
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
        boolean isManagingUser = currentUserId != null
                && (currentUserId.equals(event.getOrganizerId())
                || (event.getCoOrganizerUserIds() != null && event.getCoOrganizerUserIds().contains(currentUserId)));
        commentCanModerate = isManagingUser || isAdminView;
        commentAdapter.setOrganizerContext(
                event.getOrganizerId(),
                event.getCoOrganizerUserIds(),
                commentCanModerate,
                currentUserId);
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

    private void navigateBack() {
        boolean fromNotif = getIntent().getBooleanExtra("FROM_NOTIFICATION", false);
        if (isTaskRoot() || fromNotif) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void bindOrganizerAvatar(@Nullable User user){
        if (organizerAvatar == null) return;
        if(user == null){
            organizerAvatar.setImageResource(R.drawable.ic_person);
            return;
        }

        String url = user.getAvatarUrl();
        if (url == null || url.trim().isEmpty()){
            organizerAvatar.setImageResource(R.drawable.ic_person);
            return;
        }
        Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.bg_comment_avatar)
                .error(R.drawable.bg_comment_avatar)
                .into(organizerAvatar);
        organizerAvatar.clearColorFilter();
    }

    private void populateUI() {
        if (event == null) return;

        if(isFinishing() || isDestroyed()) return;

        if (eventPoster != null){
            eventPoster.setImageTintList(null);
            eventPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (event.hasPosterImage()) {
                String url = event.getImageUrl();
                Glide.with(this).load(url).centerCrop().into(eventPoster);
                if (isAdminView) {
                    eventPoster.setOnClickListener(v -> showAdminImageDialog());
                } else {
                    eventPoster.setOnClickListener(v -> {
                        EventPosterFragment posterFragment = EventPosterFragment.newInstance(url);
                        posterFragment.show(getSupportFragmentManager(), "EventPosterFragment");
                    });
                }
                } else {
                    Glide.with(this).clear(eventPoster);
                    eventPoster.setImageDrawable(null);
                    eventPoster.setImageTintList(null);
                    eventPoster.setOnClickListener(null);
                    if (isAdminView) {
                        eventPoster.setOnClickListener(v -> showAdminImageDialog());
                    } else {
                        eventPoster.setOnClickListener(null);

                    }
                }
            }

        eventTitle.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        if (event.getPrice() <= 0) {
            eventPrice.setText(R.string.event_price_free);
        } else {
            eventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
        }

        if (event.getTime() != null && event.getTime().getStartTime() > 0) {
            eventDate.setText(new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault()).format(new Date(event.getTime().getStartTime())));
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
            waitlistRegistrationEnds.setText(new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault()).format(new Date(event.getRegistrationEndTime())));
        } else {
            waitlistRegistrationEnds.setText("N/A");
        }


        boolean isOrganizer = currentUserId != null && currentUserId.equals(event.getOrganizerId());
        boolean isCoOrganizer = event.getCoOrganizerUserIds() != null
                && currentUserId != null
                && event.getCoOrganizerUserIds().contains(currentUserId);
        boolean isManagingUser = (isOrganizer || isCoOrganizer) && !isAdminView;

        TextView badgePrivate = findViewById(R.id.badge_private_event);
        if (badgePrivate != null) {
            badgePrivate.setVisibility(event.isPrivateEvent() ? View.VISIBLE : View.GONE);
        }

        View manageQr = findViewById(R.id.manage_row_qr);
        if (manageQr != null) {
            manageQr.setVisibility(isManagingUser && !event.isPrivateEvent() ? View.VISIBLE : View.GONE);
        }

        MaterialCardView organizerCard = findViewById(R.id.organizer_card);
        if (organizerCard != null) {
            organizerCard.setOnClickListener(v -> {
                if (event == null || event.getOrganizerId() == null || event.getOrganizerId().isEmpty()) {
                    return;
                }
                Intent profileIntent = new Intent(EventDetailViewActivity.this, PublicUserProfileActivity.class);
                profileIntent.putExtra(PublicUserProfileActivity.EXTRA_USER_ID, event.getOrganizerId());
                startActivity(profileIntent);
            });
        }

        if (isManagingUser) {
            if (isOrganizer) {
                organizerName.setText("You");
                if(event.getOrganizerId() != null){
                    userRepository.getUserById(event.getOrganizerId(), new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User result) {
                            bindOrganizerAvatar(result);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            bindOrganizerAvatar(null);

                        }
                    });
                } else{
                    bindOrganizerAvatar(null);
                }
            } else if (event.getOrganizerId() != null) {
                userRepository.getUserById(event.getOrganizerId(), new RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        organizerName.setText(user != null && user.getName() != null ? user.getName() : "Organizer");
                        bindOrganizerAvatar(user);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        organizerName.setText("Organizer");
                        bindOrganizerAvatar(null);
                    }
                });
            } else {
                organizerName.setText("Organizer");
                bindOrganizerAvatar(null);
            }
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
                        bindOrganizerAvatar(user);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        organizerName.setText("Unknown Organizer");
                        bindOrganizerAvatar(null);
                    }
                });
            } else {
                organizerName.setText("Unknown Organizer");
                bindOrganizerAvatar(null);
            }
        }

        attachCommentsListener();

        if (isInvited) {
            nestedScrollView.post(() -> {
                selectTab(1);
                scrollSectionIntoView(sectionWaitlist);
            });
        }

        if (waitlistRegistration != null) waitlistRegistration.remove();
        waitlistRegistration = new WaitlistRepository().listenToWaitlist(event.getEventId(), new RepositoryCallback<List<WaitlistEntry>>() {
            @Override
            public void onSuccess(List<WaitlistEntry> entries) {
                if (isAdminView) {

                    buttonPrimary.setVisibility(View.VISIBLE);
                    buttonPrimary.setEnabled(true);
                    buttonPrimary.setText("DELETE EVENT");
                    buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_filled));
                    buttonPrimary.setTextColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.white));

                    buttonPrimary.setOnClickListener(v -> showAdminDeleteEventDialog());

                    buttonSecondary.setVisibility(View.GONE);

                    return;
                }

                int trueCount = (entries != null) ? entries.size() : 0;
                waitlistApplicants.setText(String.valueOf(trueCount));

                boolean lotteryRun = false;
                if (event.getStatus() == com.example.zephyrevents.model.EventStatus.CLOSED) {
                    lotteryRun = true;
                }

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
                boolean beforeRegistration = event.getRegistrationStartTime() > 0 && System.currentTimeMillis() < event.getRegistrationStartTime();

                boolean isClosedForNew = trueCapacityFull || lotteryRun || pastDeadline;

                // Set Status Tag
                if (beforeRegistration) {
                    statusTag.setText("COMING SOON");
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag_orange));
                } else if (isClosedForNew) {
                    statusTag.setText("CLOSED");
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag_orange));
                } else {
                    statusTag.setText(R.string.registration_open);
                    statusTag.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_status_tag));
                }

                if (isManagingUser) {
                    if (lotteryRun) {
                        buttonPrimary.setVisibility(View.VISIBLE);
                        buttonPrimary.setEnabled(false);
                        buttonPrimary.setText("REGISTRATION CLOSED");
                        buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_muted_rounded));
                        buttonPrimary.setBackgroundTintList(null);
                        buttonPrimary.setTextColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.white));
                        buttonSecondary.setVisibility(View.GONE);
                    } else {
                        if (pastDeadline) {
                            // AUTO-RUN THE LOTTERY
                            Toast.makeText(EventDetailViewActivity.this, "Registration ended. Running lottery...", Toast.LENGTH_SHORT).show();
                            executeLottery();
                        } else {
                            buttonPrimary.setVisibility(View.VISIBLE);
                            buttonPrimary.setEnabled(true);
                            buttonPrimary.setText("RUN LOTTERY EARLY");
                            buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_filled));
                            buttonPrimary.setTextColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.white));
                            buttonPrimary.setOnClickListener(v -> {
                                AlertDialog runLotteryDialog = new MaterialAlertDialogBuilder(EventDetailViewActivity.this)
                                        .setTitle("Run Lottery")
                                        .setMessage("Are you sure you want to prematurely close registration and run the lottery?")
                                        .setPositiveButton("Run", (dialog, which) -> executeLottery())
                                        .setNegativeButton("Cancel", null)
                                        .create();
                                runLotteryDialog.show();
                                DialogUiHelper.applyCompactMaterialActions(runLotteryDialog);
                            });
                        }
                        buttonSecondary.setVisibility(View.GONE);
                    }
                } else {
                    boolean pendingPrivateInvite = !"unknown_user".equals(currentUserId)
                            && event.getPendingPrivateWaitlistInviteUserIds().contains(currentUserId);

                    boolean pendingCoOrgInvite = !"unknown_user".equals(currentUserId)
                            && event.getPendingCoOrganizerUserIds().contains(currentUserId);

                    if (pendingCoOrgInvite) {
                        showCoOrganizerInviteButtons();
                    } else if (myEntry == null && pendingPrivateInvite) {
                        showPrivateWaitlistInviteButtons();
                    } else if (myEntry == null && event.isPrivateEvent() && !pendingPrivateInvite) {
                        showPrivateEventNotInvited();
                    } else if (myEntry == null) {
                        if (beforeRegistration) {
                            buttonPrimary.setVisibility(View.VISIBLE);
                            buttonPrimary.setEnabled(false);
                            String dateStr = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(new Date(event.getRegistrationStartTime()));
                            buttonPrimary.setText("OPENS " + dateStr.toUpperCase(Locale.getDefault()));
                            buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_muted_rounded));
                            buttonPrimary.setBackgroundTintList(null);
                            buttonPrimary.setTextColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.white));
                            buttonSecondary.setVisibility(View.GONE);
                        } else if (isClosedForNew) {
                            showWaitlistClosedButton(trueCapacityFull, lotteryRun, pastDeadline);
                        } else {
                            showJoinWaitlistButton(new WaitlistRepository());
                        }
                    } else {
                        switch (myEntry.getStatus()) {
                            case WAITLISTED: showLeaveWaitlistButton(new WaitlistRepository()); break;
                            case SELECTED: showAcceptDeclineButtons(new WaitlistRepository()); break;
                            case ACCEPTED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: CONFIRMED");
                                buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_success_rounded));
                                buttonPrimary.setBackgroundTintList(null);
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                            case DECLINED:
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(false);
                                buttonPrimary.setText("STATUS: DECLINED");
                                buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_invite_declined_rounded));
                                buttonPrimary.setBackgroundTintList(null);
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                            case LOST:
                                // SECOND CHANCE WAITLIST BUTTON
                                buttonPrimary.setVisibility(View.VISIBLE);
                                buttonPrimary.setEnabled(true);
                                buttonPrimary.setText("JOIN SECOND CHANCE WAITLIST");
                                buttonPrimary.setBackground(ContextCompat.getDrawable(EventDetailViewActivity.this, R.drawable.bg_button_filled));
                                buttonPrimary.setTextColor(ContextCompat.getColor(EventDetailViewActivity.this, R.color.white));
                                buttonPrimary.setOnClickListener(v -> {
                                    new WaitlistRepository().updateStatus(event.getEventId(), currentUserId, Status.WAITLISTED, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            populateUI();
                                            String name = eventNameForWaitlistStatus();
                                            showCenteredWaitlistOverlay(
                                                    getString(R.string.status_waitlist_second_chance_title),
                                                    getString(R.string.status_waitlist_second_chance_body, name),
                                                    true);
                                        }
                                        @Override
                                        public void onFailure(Exception e) {}
                                    });
                                });
                                buttonSecondary.setVisibility(View.GONE);
                                break;
                        }
                    }
                }
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    private void showJoinWaitlistButton(WaitlistRepository repo) {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.join_waitlist);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            if (event.getLocation() != null && event.getLocation().isRequiresGeolocation()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    pendingRepo = repo;
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                } else {
                    doLocationJoin(repo);
                }
            } else {
                WaitlistEntry newEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0.0, 0.0, Status.WAITLISTED);
                repo.addUserToWaitlist(newEntry, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        populateUI();
                        String name = eventNameForWaitlistStatus();
                        showCenteredWaitlistOverlay(
                                getString(R.string.status_waitlist_on_list_title),
                                getString(R.string.status_waitlist_joined_body, name),
                                true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
            }
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
                    populateUI();
                    String name = eventNameForWaitlistStatus();
                    showCenteredWaitlistOverlay(
                            getString(R.string.status_waitlist_left_title),
                            getString(R.string.status_waitlist_left_body, name),
                            true);
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
                    navigateToLotteryInviteStatus(EventStatusActivity.STATUS_ACCEPTED);
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
                    navigateToLotteryInviteStatus(EventStatusActivity.STATUS_DECLINED);
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        });
    }

    /** Full-screen status after lottery waitlist invite accept/decline (slide up, previous screen fades). */
    private void navigateToLotteryInviteStatus(@NonNull String statusType) {
        Intent intent = new Intent(EventDetailViewActivity.this, EventStatusActivity.class);
        intent.putExtra(EventStatusActivity.EXTRA_EVENT_NAME, event.getName());
        intent.putExtra(EventStatusActivity.EXTRA_STATUS_TYPE, statusType);
        if (EventStatusActivity.STATUS_DECLINED.equals(statusType)) {
            intent.putExtra(EventStatusActivity.EXTRA_EVENT_KEY, event.getEventId());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.activity_fade_out);
        finish();
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

        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_muted_rounded));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showLostButton() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText("STATUS: NOT SELECTED");
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_muted_rounded));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showPrivateEventNotInvited() {
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText(R.string.private_event_need_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_muted_rounded));
        buttonPrimary.setBackgroundTintList(null);
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(null);
        buttonSecondary.setVisibility(View.GONE);
    }

    private void showPrivateWaitlistInviteButtons() {
        inviteContextText.setVisibility(View.VISIBLE);
        inviteContextText.setText("You have been invited to join the waitlist for a private event.");

        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.accept_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> acceptPrivateWaitlistInvite());

        buttonSecondary.setVisibility(View.VISIBLE);
        buttonSecondary.setText(R.string.decline_invite);
        buttonSecondary.setOnClickListener(v -> declinePrivateWaitlistInvite());
    }

    private void acceptPrivateWaitlistInvite() {
        if (event == null || "unknown_user".equals(currentUserId)) return;
        event.getPendingPrivateWaitlistInviteUserIds().remove(currentUserId);
        EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                WaitlistEntry newEntry = new WaitlistEntry(currentUserId, event.getEventId(), 0.0, 0.0, Status.WAITLISTED);
                new WaitlistRepository().addUserToWaitlist(newEntry, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void r) {
                        populateUI();
                        String name = eventNameForWaitlistStatus();
                        showCenteredWaitlistOverlay(
                                getString(R.string.status_waitlist_on_list_title),
                                getString(R.string.status_waitlist_invite_accepted_body, name),
                                true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EventDetailViewActivity.this, "Could not join waitlist.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Could not update event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declinePrivateWaitlistInvite() {
        if (event == null || "unknown_user".equals(currentUserId)) return;
        event.getPendingPrivateWaitlistInviteUserIds().remove(currentUserId);
        EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                populateUI();
                String name = event != null && event.getName() != null ? event.getName() : "";
                showCenteredWaitlistOverlay(
                        getString(R.string.invite_declined_title),
                        getString(R.string.invite_declined_message, name),
                        false);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EventDetailViewActivity.this, "Could not update event.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void executeLottery() {
        buttonPrimary.setEnabled(false);
        buttonPrimary.setText("RUNNING...");
        new com.example.zephyrevents.controller.LotteryController().runLottery(event.getEventId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Force deadline to NOW and set CLOSED so Cloud Function skips it
                event.setRegistrationEndTime(System.currentTimeMillis());
                event.setStatus(com.example.zephyrevents.model.EventStatus.CLOSED);

                EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void res) {
                        SystemLogController.getInstance()
                                .logAction("MANUAL_LOTTERY_RUN", "Lottery manually executed for event: '" + event.getName() + "'", organizerName.getText().toString());
                        Toast.makeText(EventDetailViewActivity.this, "Lottery complete!", Toast.LENGTH_SHORT).show();
                        populateUI();
                    }
                    @Override
                    public void onFailure(Exception e) { populateUI(); }
                });
            }
            @Override
            public void onFailure(Exception e) {
                buttonPrimary.setEnabled(true);
                buttonPrimary.setText("RUN LOTTERY");
                Toast.makeText(EventDetailViewActivity.this, "Failed to run lottery.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCoOrganizerInviteButtons() {
        inviteContextText.setVisibility(View.VISIBLE);
        inviteContextText.setText("You have been invited to Co-Organize this event.");
        buttonPrimary.setVisibility(View.VISIBLE);
        buttonPrimary.setEnabled(true);
        buttonPrimary.setText(R.string.accept_invite);
        buttonPrimary.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_filled));
        buttonPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
        buttonPrimary.setOnClickListener(v -> {
            event.getPendingCoOrganizerUserIds().remove(currentUserId);
            event.getCoOrganizerUserIds().add(currentUserId);
            EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "You are now a Co-Organizer!", Toast.LENGTH_SHORT).show();
                    populateUI(); // Refresh to show Admin tabs!
                }
                @Override public void onFailure(Exception e) {}
            });
        });

        buttonSecondary.setVisibility(View.VISIBLE);
        buttonSecondary.setText(R.string.decline_invite);
        buttonSecondary.setOnClickListener(v -> {
            event.getPendingCoOrganizerUserIds().remove(currentUserId);
            EventController.getInstance().createEvent(event, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(EventDetailViewActivity.this, "Invite Declined", Toast.LENGTH_SHORT).show();
                    populateUI();
                }
                @Override public void onFailure(Exception e) {}
            });
        });
    }

    private void doLocationJoin(WaitlistRepository repo) {
        com.example.zephyrevents.util.DistanceHelper.getUserLocation(this,
                new com.example.zephyrevents.util.DistanceHelper.LocationCallback() {
                    @Override
                    public void onLocation(double userLat, double userLng) {
                        com.example.zephyrevents.model.Coordinate userCoord =
                                new com.example.zephyrevents.model.Coordinate(userLat, userLng);
                        com.example.zephyrevents.model.Coordinate eventCoord =
                                event.getLocation().getCoordinate();
                        double radiusKm = event.getLocation().getGeolocationRadiusKm();

                        if (!com.example.zephyrevents.util.DistanceHelper.isWithinDistance(
                                userCoord, eventCoord, radiusKm)) {
                            Toast.makeText(EventDetailViewActivity.this,
                                    "You must be within " + (int)(radiusKm * 1000) + "m of the event.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        WaitlistEntry newEntry = new WaitlistEntry(
                                currentUserId, event.getEventId(), userLat, userLng, Status.WAITLISTED);
                        repo.addUserToWaitlist(newEntry, new RepositoryCallback<Void>() {
                            @Override public void onSuccess(Void result) {
                                Toast.makeText(EventDetailViewActivity.this, "Joined Waitlist!", Toast.LENGTH_SHORT).show();
                                populateUI();
                            }
                            @Override public void onFailure(Exception e) {}
                        });
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(EventDetailViewActivity.this,
                                "Could not get your location. Please enable location services.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
