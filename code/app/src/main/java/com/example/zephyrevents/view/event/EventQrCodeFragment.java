package com.example.zephyrevents.view.event;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.controller.QRController;

/**
 */
public class EventQrCodeFragment extends DialogFragment {
    private String eventID;
    private Bitmap qrBitmap;
    public EventQrCodeFragment() {}

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param id The event ID
     * @return A new instance of fragment QrCodeFragment.
     */
    public static EventQrCodeFragment newInstance(String id) {
        EventQrCodeFragment fragment = new EventQrCodeFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);

        if (getArguments() != null) {
            this.eventID = getArguments().getString("id");
            qrBitmap = QRController.generateEventQRCode(eventID, 220);  // generate asap
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make it full screen if desired
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_qr_code, container, false);

        ImageButton back = view.findViewById(R.id.button_back);
        if (back != null) back.setOnClickListener(v -> dismiss());

        ImageButton download = view.findViewById(R.id.button_download);
        if (download != null) download.setOnClickListener(v -> QRController.saveQRCodeImage(v.getContext(), qrBitmap, eventID));

        // Set QR Code
        ImageView qrImageView = view.findViewById(R.id.image_qr_code);
        if (qrBitmap != null) {
            qrImageView.setImageBitmap(qrBitmap);
        }

        return view;
    }
}