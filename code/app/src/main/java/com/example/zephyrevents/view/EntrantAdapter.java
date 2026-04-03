package com.example.zephyrevents.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Entrant;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrants;
    private EntrantCancelListener cancelListener;

    public EntrantAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
        this.cancelListener = null;
    }

    public EntrantAdapter(List<Entrant> entrants, EntrantCancelListener listener) {
        this.entrants = entrants;
        this.cancelListener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);

        holder.nameText.setText(entrant.name);
        holder.detailText.setText(entrant.detail);

        String initial = entrant.name.isEmpty() ? "?" : entrant.name.substring(0, 1).toUpperCase();
        holder.initialText.setText(initial);

        if (entrant.showCancel) {
            holder.cancelBtn.setVisibility(View.VISIBLE);
        } else {
            holder.cancelBtn.setVisibility(View.GONE);
        }

        holder.cancelBtn.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancelRequested(entrant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView detailText;
        TextView initialText;
        Button cancelBtn;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_entrant_name);
            detailText = itemView.findViewById(R.id.text_entrant_detail);
            initialText = itemView.findViewById(R.id.text_entrant_initial); // NEW
            cancelBtn = itemView.findViewById(R.id.btn_cancel_entrant);
        }
    }

    public interface EntrantCancelListener {
        void onCancelRequested(Entrant entrant);
    }
}