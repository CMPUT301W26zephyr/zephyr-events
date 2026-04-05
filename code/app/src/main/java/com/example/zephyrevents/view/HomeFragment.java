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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.util.HomeExploreConstants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
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

    private static final int MAX_CATEGORY_ITEMS = 15;

    private LinearLayout categoriesContainer;
    private final List<Event> closingSoonEvents = new ArrayList<>();
    private final List<Event> trendingEvents = new ArrayList<>();
    private final List<Event> newOnLottofyEvents = new ArrayList<>();
    private final List<Event> freeEvents = new ArrayList<>();
    private HomeCategoryEventAdapter closingSoonAdapter;
    private HomeCategoryEventAdapter trendingAdapter;
    private HomeCategoryEventAdapter newOnLottofyAdapter;
    private HomeCategoryEventAdapter freeAdapter;
    private View sectionClosingSoon;
    private View sectionTrending;
    private View sectionNewOnLottofy;
    private View sectionFree;

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
        categoriesContainer = view.findViewById(R.id.categories_container);

        setupCarousel();
        setupCategorySections();

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

        carousel.post(() -> {
            View pagerChild = carousel.getChildAt(0);
            if (pagerChild instanceof RecyclerView) {
                ((RecyclerView) pagerChild).setNestedScrollingEnabled(false);
            }
        });

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

    private void setupCategorySections() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        closingSoonAdapter = new HomeCategoryEventAdapter(closingSoonEvents, this::onCategoryEventClick);
        sectionClosingSoon = inflater.inflate(R.layout.widget_home_category_row, categoriesContainer, false);
        bindCategorySection(sectionClosingSoon, R.string.home_category_closing_soon, closingSoonAdapter,
                EventsListFragment.HomeListCategory.CLOSING_SOON);
        categoriesContainer.addView(sectionClosingSoon);

        trendingAdapter = new HomeCategoryEventAdapter(trendingEvents, this::onCategoryEventClick);
        sectionTrending = inflater.inflate(R.layout.widget_home_category_row, categoriesContainer, false);
        bindCategorySection(sectionTrending, R.string.home_category_trending, trendingAdapter,
                EventsListFragment.HomeListCategory.TRENDING);
        categoriesContainer.addView(sectionTrending);

        newOnLottofyAdapter = new HomeCategoryEventAdapter(newOnLottofyEvents, this::onCategoryEventClick);
        sectionNewOnLottofy = inflater.inflate(R.layout.widget_home_category_row, categoriesContainer, false);
        bindCategorySection(sectionNewOnLottofy, R.string.home_category_new, newOnLottofyAdapter,
                EventsListFragment.HomeListCategory.NEW_WITHIN_7_DAYS);
        categoriesContainer.addView(sectionNewOnLottofy);

        freeAdapter = new HomeCategoryEventAdapter(freeEvents, this::onCategoryEventClick);
        sectionFree = inflater.inflate(R.layout.widget_home_category_row, categoriesContainer, false);
        bindCategorySection(sectionFree, R.string.home_category_free, freeAdapter,
                EventsListFragment.HomeListCategory.FREE);
        categoriesContainer.addView(sectionFree);
    }

    private void bindCategorySection(@NonNull View section, int titleRes,
                                     @NonNull HomeCategoryEventAdapter adapter,
                                     @NonNull EventsListFragment.HomeListCategory seeAllCategory) {
        TextView title = section.findViewById(R.id.category_title);
        title.setText(titleRes);
        TextView seeAll = section.findViewById(R.id.category_see_all);
        seeAll.setOnClickListener(v -> openEventsList(seeAllCategory));
        RecyclerView recycler = section.findViewById(R.id.category_recycler);
        recycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recycler.setAdapter(adapter);
    }

    private void onCategoryEventClick(Event event) {
        if (event.getEventId() != null) {
            boolean invited = eventController.isInvitedEvent(event.getEventId());
            openEventDetail(event.getEventId(), invited);
        }
    }

    private void openEventsList() {
        openEventsList(EventsListFragment.HomeListCategory.NONE);
    }

    private void openEventsList(@NonNull EventsListFragment.HomeListCategory category) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_container, EventsListFragment.newInstance(category))
                .addToBackStack(null)
                .commit();
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
            tvViewAll.setOnClickListener(v -> openEventsList());
        }

        // Search icon does the same thing as the textView for now
        // TODO: Possible to make it open the search box? Via intent
        ImageView searchIcon = view.findViewById(R.id.btn_search);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> openEventsList());
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
     */
    private void loadFeaturedEvents() {
        progressBar.setVisibility(View.VISIBLE);

        eventController.getAllEvents(new RepositoryCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                featuredEvents.clear();

                List<Event> publicEvents = new ArrayList<>();
                if (result != null) {
                    for (Event e : result) {
                        if (e != null && !e.isPrivateEvent()) {
                            publicEvents.add(e);
                        }
                    }
                }

                if (!publicEvents.isEmpty()) {
                    publicEvents.sort((e1, e2) -> Double.compare(eventScore(e2), eventScore(e1)));
                    int numCarousel = Math.min(numMaxCarousel, publicEvents.size());
                    for (int i = 0; i < numCarousel; i++) {
                        featuredEvents.add(publicEvents.get(i));
                    }
                }

                populateCategoryLists(publicEvents);
                refreshCategoryUi();

                cancelSlowScroll();
                adapter.notifyDataSetChanged();
                finishLoading();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to load featured events.", Toast.LENGTH_SHORT).show();
                featuredEvents.clear();
                populateCategoryLists(new ArrayList<>());
                refreshCategoryUi();
                cancelSlowScroll();
                adapter.notifyDataSetChanged();
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
        String desc = event.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            score += 8 + Math.min(desc.length()/5, 12);
        }

        // TODO: add more heuristics for featured events
        return score;
    }

    private void populateCategoryLists(List<Event> publicEvents) {
        long now = System.currentTimeMillis();
        long closingDeadline = now + HomeExploreConstants.CLOSING_SOON_MS;

        closingSoonEvents.clear();
        for (Event e : publicEvents) {
            long regEnd = e.getRegistrationEndTime();
            if (regEnd > now && regEnd <= closingDeadline) {
                closingSoonEvents.add(e);
            }
        }
        closingSoonEvents.sort((a, b) -> Long.compare(a.getRegistrationEndTime(), b.getRegistrationEndTime()));
        trimToMax(closingSoonEvents);

        trendingEvents.clear();
        trendingEvents.addAll(publicEvents);
        trendingEvents.sort((a, b) -> Integer.compare(b.getCurrentApplicants(), a.getCurrentApplicants()));
        trimToMax(trendingEvents);

        newOnLottofyEvents.clear();
        for (Event e : publicEvents) {
            long regStart = e.getRegistrationStartTime();
            if (regStart > 0
                    && regStart <= now
                    && now - regStart <= HomeExploreConstants.NEW_ON_LOTTOFY_MAX_AGE_MS) {
                newOnLottofyEvents.add(e);
            }
        }
        newOnLottofyEvents.sort((a, b) ->
                Long.compare(b.getRegistrationStartTime(), a.getRegistrationStartTime()));
        trimToMax(newOnLottofyEvents);

        freeEvents.clear();
        for (Event e : publicEvents) {
            if (e.getPrice() <= 0) {
                freeEvents.add(e);
            }
        }
        freeEvents.sort((a, b) -> Integer.compare(b.getCurrentApplicants(), a.getCurrentApplicants()));
        trimToMax(freeEvents);
    }

    private static void trimToMax(List<Event> list) {
        while (list.size() > MAX_CATEGORY_ITEMS) {
            list.remove(list.size() - 1);
        }
    }

    private void refreshCategoryUi() {
        if (sectionClosingSoon == null) {
            return;
        }
        sectionClosingSoon.setVisibility(closingSoonEvents.isEmpty() ? View.GONE : View.VISIBLE);
        sectionTrending.setVisibility(trendingEvents.isEmpty() ? View.GONE : View.VISIBLE);
        sectionNewOnLottofy.setVisibility(newOnLottofyEvents.isEmpty() ? View.GONE : View.VISIBLE);
        sectionFree.setVisibility(freeEvents.isEmpty() ? View.GONE : View.VISIBLE);

        closingSoonAdapter.notifyDataSetChanged();
        trendingAdapter.notifyDataSetChanged();
        newOnLottofyAdapter.notifyDataSetChanged();
        freeAdapter.notifyDataSetChanged();
    }

    private void openEventDetail(String eventKey, boolean invited) {
        Intent intent = new Intent(requireContext(), EventDetailViewActivity.class);
        intent.putExtra(EventDetailViewActivity.EXTRA_EVENT, eventKey);
        intent.putExtra(EventDetailViewActivity.EXTRA_INVITED, invited);
        startActivity(intent);
    }
}