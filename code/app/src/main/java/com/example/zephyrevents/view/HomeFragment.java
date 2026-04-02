package com.example.zephyrevents.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HomeActivity is the default first screen after the authentication (log in) step
 * Shows large cards of featured events (e.g. by popularity, proximity)
 * Checks if User account still exists upon creation; returns to WelcomeActivity if not exist.
 */
public class HomeFragment extends Fragment {
    private UserController userController;
    private EventController eventController;

    private FeaturedEventPagerAdapter adapter;
    private List<Event> featuredEvents = new ArrayList<>();

    private ViewPager2 carousel;
    private TabLayout tabLayoutDots;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ValueAnimator scrollAnimator;
    private final int numMaxCarousel = 5;

    // Auto scroll
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null && adapter.getItemCount() > 0) {
                slowScrollToNextItem();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false); // Rename your layout files if you want
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventController = EventController.getInstance();
        userController = new UserController(requireContext());

        carousel = view.findViewById(R.id.carousel_featured);
        tabLayoutDots = view.findViewById(R.id.tab_layout_dots);
        progressBar = view.findViewById(R.id.progress_bar_featured);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        setupCarousel();

        progressBar.setVisibility(View.VISIBLE);
        setupClickListeners(view);
        verifyUserSession();

        swipeRefreshLayout.setOnRefreshListener(this::loadFeaturedEvents);
        loadFeaturedEvents();
    }

    /**
     * Sets up carousel adapter and click listener
     */
    private void setupCarousel() {
        adapter = new FeaturedEventPagerAdapter(featuredEvents,
            event -> {
                if (event.getEventId() != null) {
                    boolean invited = eventController.isInvitedEvent(event.getEventId());
                    openEventDetail(event.getEventId(), invited);
                }
            }
            );
        carousel.setAdapter(adapter);

        // Styling
        carousel.setOffscreenPageLimit(3);

        int overlapPx = (int) (80 * getResources().getDisplayMetrics().density);  // makes 30 dp

        carousel.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);

            // Scale down side cards
            float scale = 0.85f + (1 - absPosition) * 0.15f;
            scale = Math.max(0.85f, scale); // clamp
            page.setScaleY(scale);
            page.setScaleX(scale);

            // Fade out side cards
            float alpha = 0.3f + (1 - absPosition) * 0.7f;
            page.setAlpha(Math.max(0.4f, alpha));

            // Overlap cards horizontally
            // If position is positive, card is on right, we translate it left (negative)
            page.setTranslationX(-position * overlapPx);

            // Center card on top
            page.setTranslationZ(1 - absPosition);
        });

        // https://developer.android.com/reference/com/google/android/material/tabs/TabLayoutMediator
        // mediator syncs selection state
        new TabLayoutMediator(tabLayoutDots, carousel, (tab, position) -> {}).attach();

        // Control the auto-scroll behavior when user interacts
        carousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                startAutoScroll(); // Reset timer so it doesn't jump immediately after manual swipe
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopAutoScroll(); // Pause scrolling while user is dragging
                }
            }
        });
    }

    private void startAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, 5000); // 5 seconds
    }

    private void stopAutoScroll() {
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void slowScrollToNextItem() {
        if (adapter == null || adapter.getItemCount() == 0) return;

        int currentItem = carousel.getCurrentItem();
        int nextItem = (currentItem + 1) % adapter.getItemCount();

        if (carousel.beginFakeDrag()) {

            int cardWidth = carousel.getWidth()
                    - carousel.getPaddingLeft()
                    - carousel.getPaddingRight();

            int totalDragDistance;
            long duration;

            if (nextItem == 0) {
                totalDragDistance = cardWidth * currentItem;
                duration = 1200L; // more time since it travels further
            } else {
                totalDragDistance = -cardWidth;
                duration = 800L;
            }
            scrollAnimator = ValueAnimator.ofInt(0, totalDragDistance);
            scrollAnimator.setDuration(duration);
            scrollAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

            // Keep track of the last value to calculate the delta
            final int[] previousValue = {0};

            scrollAnimator.addUpdateListener(valueAnimator -> {
                int currentValue = (int) valueAnimator.getAnimatedValue();
                float currentPxToDrag = (float) (currentValue - previousValue[0]);

                carousel.fakeDragBy(currentPxToDrag);
                previousValue[0] = currentValue;
            });

            scrollAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (carousel.isFakeDragging()) {
                        carousel.endFakeDrag();
                    }
                }
            });
            scrollAnimator.start();
        }
    }

    private void cancelSlowScroll() {
        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
        if (carousel != null && carousel.isFakeDragging()) {
            carousel.endFakeDrag();
        }
    }

    /**
     * Sets up click listeners for the search icon and "view all events" icon.
     * @param view The view context requried
     */
    private void setupClickListeners(View view) {
        TextView tvViewAll = view.findViewById(R.id.view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(R.id.fragment_container, new EventsListFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Search icon does the same thing as the textView for now
        // TODO: Possible to make it open the search box? Via intent
        ImageView searchIcon = view.findViewById(R.id.btn_search);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                // Swap to EventsListFragment and add it to the back stack
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(R.id.fragment_container, new EventsListFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void verifyUserSession() {
        // Fetch user data in the background to verify that account still exists
        userController.fetchCurrentUser(new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                // User exists! Can do Personalized UI updates here (e.g. Welcome, <Name>!)
            }

            @Override
            public void onFailure(Exception e) {
                // Check if the controller wiped the session because the doc was missing
                if (!userController.isUserLoggedIn()) {
                    // Kick them back to WelcomeActivity
                    Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                    intent.putExtra("TOAST_MESSAGE", "Your Account Has Been Removed.");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    // If it was just a normal network error (e.g., user is offline), do nothing (or something, idrk)
                }
            }
        });
    }

    // start and stop scroll when coming back from other activity
    @Override
    public void onResume() {
        super.onResume();
        if (carousel != null) carousel.requestTransform();
        startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    private void finishLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        carousel.setCurrentItem(0);
        carousel.requestTransform();
    }

    /**
     * Loads featured events
     * NOTE: Placeholder logic currently just picks three random events.
     */
    private void loadFeaturedEvents() {
        progressBar.setVisibility(View.VISIBLE);

        eventController.getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                featuredEvents.clear();

                if (result != null && !result.isEmpty()) {
                    List<Event> publicEvents = new ArrayList<>();
                    for (Event e : result) {
                        if (e != null && !e.isPrivateEvent()) {
                            publicEvents.add(e);
                        }
                    }
                    // Select top 3 events based on score
                    publicEvents.sort((e1, e2) -> Double.compare(eventScore(e2), eventScore(e1)));
                    int numCarousel = Math.min(numMaxCarousel, publicEvents.size());
                    for (int i = 0; i < numCarousel; i++) {
                        featuredEvents.add(publicEvents.get(i));
                    }
                }
                cancelSlowScroll();
                adapter.notifyDataSetChanged();
                finishLoading();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to load featured events.", Toast.LENGTH_SHORT).show();
                finishLoading();
            }
        });
    }

    /**
     * Scores an event based on image availability, used for carousel selection
     * @param event The event
     */
    private double eventScore(Event event) {
        double score = Math.random() * 30;

        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
            score += 50;  // favor images
        }
        // TODO: add more heuristics for featured events
        return score;
    }


    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(requireContext(), EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}