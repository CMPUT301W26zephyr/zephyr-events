package com.example.zephyrevents.view;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyrevents.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * Instagram/TikTok-style bottom sheet with text field and Post action, lifted above the keyboard.
 */
public final class CommentComposeBottomSheet {

    public interface PostListener {
        void onPost(@Nullable String text);
    }

    private CommentComposeBottomSheet() {}

    public static void show(
            AppCompatActivity activity,
            String title,
            @Nullable String subtitle,
            String avatarLetter,
            PostListener listener) {

        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View root = inflater.inflate(R.layout.dialog_comment_compose, null, false);
        dialog.setContentView(root);

        TextView titleView = root.findViewById(R.id.compose_title);
        TextView subtitleView = root.findViewById(R.id.compose_subtitle);
        TextView letterView = root.findViewById(R.id.compose_avatar_letter);
        EditText editText = root.findViewById(R.id.compose_edit_text);
        TextView post = root.findViewById(R.id.compose_post);

        titleView.setText(title);
        if (TextUtils.isEmpty(subtitle)) {
            subtitleView.setVisibility(android.view.View.GONE);
        } else {
            subtitleView.setVisibility(android.view.View.VISIBLE);
            subtitleView.setText(subtitle);
        }
        String letter = TextUtils.isEmpty(avatarLetter) ? "?" : avatarLetter.substring(0, 1);
        letterView.setText(letter.toUpperCase(java.util.Locale.getDefault()));

        post.setOnClickListener(v -> {
            String t = editText.getText() != null ? editText.getText().toString().trim() : "";
            if (t.isEmpty()) {
                Toast.makeText(activity, R.string.comment_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onPost(t);
            dialog.dismiss();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
        editText.requestFocus();
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
