package com.example.zephyrevents.view.admin;

import android.content.Intent;

import androidx.fragment.app.Fragment;

public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
        // empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        // move to AdminHomeActivity
        startActivity(new Intent(requireContext(), AdminHomeActivity.class));

        // when return, remove fragment.
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}