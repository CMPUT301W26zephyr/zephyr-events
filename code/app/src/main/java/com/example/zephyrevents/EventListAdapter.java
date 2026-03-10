package com.example.zephyrevents;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.zephyrevents.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder>   {
    private List<Event> events = new ArrayList<>();

    public EventListAdapter(List<Event> events){
        this.events = events;
    }

    public void updateEvents(List<Event> events){
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_card, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Event event = events.get(position);
        holder.txtTitle.setText(event.getName() != null ? event.getName() : "");
        holder.txtDescription.setText(event.getDescription() != null ? event.getDescription() : "");

    }

    @Override

    public int getItemCount(){
        return events.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtDescription;
        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtEventTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }

}
