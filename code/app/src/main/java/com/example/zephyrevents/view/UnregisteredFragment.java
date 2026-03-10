package com.example.zephyrevents.view;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Entrant;

import java.util.ArrayList;
import java.util.List;

public class UnregisteredFragment extends Fragment implements EntrantAdapter.EntrantCancelListener {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unregistered, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNotify = view.findViewById(R.id.btn_notify);
        if (btnNotify != null) btnNotify.setText("NOTIFY");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_unregistered);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Entrant> mockWaitlist = new ArrayList<>();
        mockWaitlist.add(new Entrant("Ghost", "Hallownest", true));
        mockWaitlist.add(new Entrant("Elderbug", "Dirtmouth", true));
        mockWaitlist.add(new Entrant("Godseeker", "Godhome", true));

        EntrantAdapter adapter = new EntrantAdapter(mockWaitlist, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCancelRequested(Entrant entrant) {
        showConfirmationPopup(entrant);
    }

    private void showConfirmationPopup(Entrant entrant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View popupView = getLayoutInflater().inflate(R.layout.confirm_popup, null);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleText = popupView.findViewById(R.id.text_popup_title);
        if(titleText != null) titleText.setText("Confirm cancel entrant");

        TextView descText = popupView.findViewById(R.id.text_popup_desc);
        if(descText != null) descText.setText("Are you sure you want to cancel " + entrant.name + "?");

        Button btnCancel = popupView.findViewById(R.id.btn_popup_cancel);
        if(btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = popupView.findViewById(R.id.btn_popup_confirm);
        if(btnConfirm != null) {
            btnConfirm.setText("YES");
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}