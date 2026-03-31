package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.EventController;
import com.example.zephyrevents.controller.UserController;
import com.example.zephyrevents.model.Event;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.model.User;

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

        setupCarousel();

        setupClickListeners(view);

        verifyUserSession();

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
        carousel.setOffscreenPageLimit(5);
        carousel.setPageTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });

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

    // Refresh featured events every time the screen becomes visible
    @Override
    public void onResume() {
        super.onResume();
        loadFeaturedEvents();
    }

    /**
     * Loads featured events
     * NOTE: Placeholder logic currently just picks three random events.
     */
    private void loadFeaturedEvents() {
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
                    int max = Math.min(3, publicEvents.size());
                    for (int i = 0; i < max; i++) {
                        featuredEvents.add(publicEvents.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to load featured events.", Toast.LENGTH_SHORT).show();
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