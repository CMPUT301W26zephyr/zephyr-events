package com.example.zephyrevents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterCategoryAdapter extends RecyclerView.Adapter<FilterCategoryAdapter.ViewHolder> {
private static final String[] CATEGORIES = {"Food", "Sports", "Arts"};
private final Set<Integer> selected = new HashSet<>();

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_category, parent, false);
    return new ViewHolder(view);
}
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.txtCategory.setText(CATEGORIES[position]);
    holder.itemView.setOnClickListener(v -> {
        int pos = holder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            if (selected.contains(pos)) selected.remove(pos);
            else selected.add(pos);
            notifyItemChanged(pos);
        }
    });
}
@Override
public int getItemCount() {
    return CATEGORIES.length;
}
public List<String> getSelectedCategories() {
    List<String> list = new ArrayList<>();
    for (int i : selected) list.add(CATEGORIES[i]);
    return list;
}
static class ViewHolder extends RecyclerView.ViewHolder {
    TextView txtCategory;
    ViewHolder(View itemView) {
        super(itemView);
        txtCategory = itemView.findViewById(R.id.txtCategory);
    }
}



}
