package com.example.zephyrevents.view;

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Dialog that shows the Terms of Service
 * Defines callback interface for host activity to receive "Confirm" button pressed.
 */
public class TermsOfServiceFragment extends DialogFragment {

    // Interface to communicate back to the Activity
    public interface OnTosAgreedListener {
        void onTosAgreed();
    }

    private OnTosAgreedListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 2. Full screen
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the hosting Activity implements the interface
        if (context instanceof OnTosAgreedListener) {
            listener = (OnTosAgreedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTosAgreedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Assuming your layout is named fragment_terms_of_service.xml
        View view = inflater.inflate(R.layout.fragment_terms_of_service, container, false);

        Button btnAgree = view.findViewById(R.id.btn_agree);

        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger the callback to check the box
                if (listener != null) {
                    listener.onTosAgreed();
                }
                // Close the bottom sheet
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}