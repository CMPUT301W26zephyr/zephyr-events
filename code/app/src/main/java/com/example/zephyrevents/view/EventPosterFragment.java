package com.example.zephyrevents.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;

public class EventPosterFragment extends DialogFragment {

    private String imageUrl;

    public static EventPosterFragment newInstance(String imageUrl) {
        EventPosterFragment fragment = new EventPosterFragment();
        Bundle args = new Bundle();
        args.putString("image_url", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_NoActionBar);  // NOTE: Use this for full screen

        if (getArguments() != null) {
            imageUrl = getArguments().getString("image_url");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_poster, container, false);

        ImageButton btnClose = view.findViewById(R.id.button_close);
        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());

        ImageView imageView = view.findViewById(R.id.image_poster_fullscreen);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);
        }

        return view;
    }
}