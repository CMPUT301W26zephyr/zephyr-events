package com.example.zephyrevents.util;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.zephyrevents.R;

/**
 * Shared styling for Material alert dialogs (compact action buttons, etc.).
 */
public final class DialogUiHelper {

    private DialogUiHelper() {}

    /** Slightly shorter positive/negative/neutral buttons to match custom dialog layouts. */
    public static void applyCompactMaterialActions(@Nullable AlertDialog dialog) {
        if (dialog == null) {
            return;
        }
        int minH = dialog.getContext().getResources().getDimensionPixelSize(R.dimen.dialog_action_button_height);
        for (int which : new int[]{
                AlertDialog.BUTTON_POSITIVE,
                AlertDialog.BUTTON_NEGATIVE,
                AlertDialog.BUTTON_NEUTRAL
        }) {
            Button b = dialog.getButton(which);
            if (b != null) {
                b.setMinHeight(minH);
                b.setMinimumHeight(minH);
            }
        }
        applyStandardActionColors(dialog);
    }

    /** Cancel/dismiss (negative/neutral): grey; primary action (positive): brand pink. */
    public static void applyStandardActionColors(@Nullable AlertDialog dialog) {
        if (dialog == null) {
            return;
        }
        int grey = ContextCompat.getColor(dialog.getContext(), R.color.text_secondary);
        int pink = ContextCompat.getColor(dialog.getContext(), R.color.primary_red);
        Button neg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neu = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (neg != null) {
            neg.setTextColor(grey);
        }
        if (neu != null) {
            neu.setTextColor(grey);
        }
        if (pos != null) {
            pos.setTextColor(pink);
        }
    }

    /** Sets title and body on {@link R.layout#admin_delete_yesorno} for contextual admin delete confirmations. */
    public static void bindAdminDeleteContent(@Nullable View root, @StringRes int titleRes, @StringRes int messageRes) {
        if (root == null) {
            return;
        }
        TextView title = root.findViewById(R.id.admin_delete_title);
        TextView message = root.findViewById(R.id.admin_delete_message);
        if (title != null) {
            title.setText(titleRes);
        }
        if (message != null) {
            message.setText(messageRes);
        }
    }
}
