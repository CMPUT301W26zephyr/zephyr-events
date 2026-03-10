package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.zephyrevents.R;

public class FinalListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_final_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnNotify = view.findViewById(R.id.btn_notify);
        if (btnNotify != null) btnNotify.setText("NOTIFY");

        Button btnExport = view.findViewById(R.id.btn_export);
        if (btnExport != null) btnExport.setText("EXPORT CSV");
    }
}