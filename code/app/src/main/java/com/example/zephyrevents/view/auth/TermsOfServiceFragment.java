package com.example.zephyrevents.view.auth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.zephyrevents.R;

// Presents terms of service during onboarding or consent
/**
 * Full-screen terms dialog. Sign-up flow uses {@link OnTosAgreedListener} and “I agree”.
 * Profile and other screens can open in read-only mode with {@link #newReadOnly()}.
 */
public class TermsOfServiceFragment extends DialogFragment {

    public static final String ARG_READ_ONLY = "read_only";

    public interface OnTosAgreedListener {
        void onTosAgreed();
    }

    private OnTosAgreedListener listener;
    private boolean readOnly;

    public static TermsOfServiceFragment newReadOnly() {
        TermsOfServiceFragment f = new TermsOfServiceFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_READ_ONLY, true);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // onAttach runs before onCreate — read args here so read-only mode does not require OnTosAgreedListener.
        Bundle args = getArguments();
        readOnly = args != null && args.getBoolean(ARG_READ_ONLY, false);
        if (!readOnly) {
            if (context instanceof OnTosAgreedListener) {
                listener = (OnTosAgreedListener) context;
            } else {
                throw new RuntimeException(context + " must implement OnTosAgreedListener");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms_of_service, container, false);

        Button btnAgree = view.findViewById(R.id.btn_agree);
        btnAgree.setText(readOnly ? R.string.terms_close : R.string.tos_i_agree);
        btnAgree.setOnClickListener(v -> {
            if (!readOnly && listener != null) {
                listener.onTosAgreed();
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
