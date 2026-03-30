package com.example.zephyrevents.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.QRController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Notification;
import com.example.zephyrevents.repository.NotificationRepository;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.util.LocalNotificationHelper;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment myEventsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;
    private final FragmentManager fm = getSupportFragmentManager();
    private ListenerRegistration notificationListener;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Please enable notifications in settings to receive updates.", Toast.LENGTH_SHORT).show();
                }
            });


    // QR Code scanner
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    Intent eventIntent = QRController.getEventIntentFromUri(this, result.getContents());

                    if (eventIntent != null) {
                        startActivity(eventIntent);
                    } else {
                        Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                    }                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            myEventsFragment = new MyEventsFragment();
            profileFragment = new UserProfileViewFragment();
            activeFragment = homeFragment;

            setupFragments();
        } else {  // Handle reloads after rotations
            restoreFragmentReferences();
        }
        setupBottomNav();

        if (savedInstanceState == null) {
            routeIntent(getIntent(), null);
        }

        updateBottomNavColors(activeFragment);

        requestNotificationPermission();
        startListeningForNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the listener to prevent memory leaks
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        routeIntent(intent, null);
    }

    private void routeIntent(Intent intent, Bundle savedInstanceState) {
        String targetTab = intent.getStringExtra("TARGET_TAB");

        if ("MyEvents".equals(targetTab)) {
            switchFragment(myEventsFragment);
        } else if ("ProfileView".equals(targetTab)) {
            switchFragment(profileFragment);
        } else if (savedInstanceState == null) {
            switchFragment(homeFragment);
        }
    }

    private void setupFragments() {
        fm.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, myEventsFragment, "myEvents").hide(myEventsFragment)
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_home).setOnClickListener(v -> switchFragment(homeFragment));
        findViewById(R.id.nav_my_events).setOnClickListener(v -> switchFragment(myEventsFragment));
        findViewById(R.id.nav_profile).setOnClickListener(v -> switchFragment(profileFragment));

        findViewById(R.id.nav_create_event).setOnClickListener(v -> {
            startActivity(new Intent(this, OrganizerEventAddEditView.class));
        });

        findViewById(R.id.nav_scan_qr).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan an Event QR Code");
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setBeepEnabled(false);
            options.setOrientationLocked(true);
            options.setCaptureActivity(QrScannerActivity.class);
            barcodeLauncher.launch(options);
        });
    }

    private void switchFragment(Fragment target) {
        if (activeFragment == target) return; // if already on target tab
        while (fm.getBackStackEntryCount() > 0) {  // Exit any nested fragments
            fm.popBackStackImmediate();
        }
        fm.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();

        activeFragment = target;
        updateBottomNavColors(target);
    }

    private void updateBottomNavColors(Fragment fragment) {
        ImageButton navHome = findViewById(R.id.nav_home);
        ImageButton navMyEvents = findViewById(R.id.nav_my_events);
        ImageButton navProfile = findViewById(R.id.nav_profile);

        int activeColor = ContextCompat.getColor(this, R.color.primary_red);
        int inactiveColor = Color.parseColor("#757575");

        navHome.setImageTintList(ColorStateList.valueOf(inactiveColor));
        navMyEvents.setImageTintList(ColorStateList.valueOf(inactiveColor));
        navProfile.setImageTintList(ColorStateList.valueOf(inactiveColor));

        if (fragment instanceof HomeFragment) {
            navHome.setImageTintList(ColorStateList.valueOf(activeColor));
        } else if (fragment instanceof MyEventsFragment) {
            navMyEvents.setImageTintList(ColorStateList.valueOf(activeColor));
        } else if (fragment instanceof UserProfileViewFragment) {
            navProfile.setImageTintList(ColorStateList.valueOf(activeColor));
        }
    }

    private void restoreFragmentReferences() {
        homeFragment = fm.findFragmentByTag("home");
        myEventsFragment = fm.findFragmentByTag("myEvents");
        profileFragment = fm.findFragmentByTag("profile");

        if (homeFragment != null && !homeFragment.isHidden()) activeFragment = homeFragment;
        else if (myEventsFragment != null && !myEventsFragment.isHidden()) activeFragment = myEventsFragment;
        else if (profileFragment != null && !profileFragment.isHidden()) activeFragment = profileFragment;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void startListeningForNotifications() {
        String userId = new UserController(this).getCurrentUserId();
        if (userId == null) return;

        NotificationRepository notifRepo = new NotificationRepository();

        // Listen for ANY changes to this user's notifications in real-time
        notificationListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        // Look for new or modified notifications
                        if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                            Notification notif = dc.getDocument().toObject(Notification.class);

                            // If we haven't 'sent' the OS popup for this yet
                            if (!notif.isSent()) {
                                // 1. Show the OS Popup!
                                LocalNotificationHelper.showNotification(
                                        this,
                                        "Zephyr Events Update",
                                        notif.getText()
                                );

                                // 2. Mark it as sent in Firebase so we don't pop it up again
                                notif.setSent(true);
                                notifRepo.updateNotification(notif, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) { /* Successfully marked as sent */ }
                                    @Override
                                    public void onFailure(Exception ex) { /* Handle failure if needed */ }
                                });
                            }
                        }
                    }
                });
    }
}