package com.example.zephyrevents.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.Entrant;

import java.util.List;

// Presents entrants (waitlist/lottery views) to organizers with per-row actions; Adapter pattern between entrant data and the list UI.
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrants;
    private EntrantCancelListener cancelListener;
    @Nullable
    private EntrantProfileClickListener profileClickListener;

    public EntrantAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
        this.cancelListener = null;
    }

    public EntrantAdapter(List<Entrant> entrants, EntrantCancelListener listener) {
        this.entrants = entrants;
        this.cancelListener = listener;
    }

    public EntrantAdapter(List<Entrant> entrants, EntrantCancelListener cancelListener,
                          @Nullable EntrantProfileClickListener profileClickListener) {
        this.entrants = entrants;
        this.cancelListener = cancelListener;
        this.profileClickListener = profileClickListener;
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

        if (!TextUtils.isEmpty(entrant.avatarUrl)) {
            holder.initialText.setVisibility(View.GONE);
            holder.avatarImage.setVisibility(View.VISIBLE);
            Glide.with(holder.avatarImage.getContext())
                    .load(entrant.avatarUrl.trim())
                    .circleCrop()
                    .placeholder(R.drawable.bg_comment_avatar)
                    .error(R.drawable.bg_comment_avatar)
                    .into(holder.avatarImage);
        } else {
            holder.avatarImage.setVisibility(View.GONE);
            holder.initialText.setVisibility(View.VISIBLE);
            Glide.with(holder.avatarImage.getContext()).clear(holder.avatarImage);
        }

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

        holder.itemView.setOnClickListener(v -> {
            if (profileClickListener == null || TextUtils.isEmpty(entrant.userId)) {
                return;
            }
            profileClickListener.onProfileClick(entrant);
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
        ImageView avatarImage;
        Button cancelBtn;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_entrant_name);
            detailText = itemView.findViewById(R.id.text_entrant_detail);
            initialText = itemView.findViewById(R.id.text_entrant_initial);
            avatarImage = itemView.findViewById(R.id.entrant_avatar_image);
            cancelBtn = itemView.findViewById(R.id.btn_cancel_entrant);
        }
    }

    public interface EntrantCancelListener {
        void onCancelRequested(Entrant entrant);
    }

    public interface EntrantProfileClickListener {
        void onProfileClick(Entrant entrant);
    }
}