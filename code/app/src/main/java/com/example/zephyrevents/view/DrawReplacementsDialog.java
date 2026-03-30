package com.example.zephyrevents.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.LotteryController;
import com.example.zephyrevents.repository.RepositoryCallback;

public class DrawReplacementsDialog extends DialogFragment {

    private String eventId;

    public static DrawReplacementsDialog newInstance(String eventId) {
        DrawReplacementsDialog dialog = new DrawReplacementsDialog();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.dialog_draw_replacements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button confirmBtn = view.findViewById(R.id.btn_draw_confirm);
        confirmBtn.setOnClickListener(v -> {
            confirmBtn.setEnabled(false);
            confirmBtn.setText("DRAWING...");

            new LotteryController().drawReplacements(eventId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(requireContext(), "Replacements Drawn!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(requireContext(), "Failed to draw replacements.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        });
    }
}