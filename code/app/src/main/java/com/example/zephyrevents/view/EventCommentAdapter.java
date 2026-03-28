package com.example.zephyrevents.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Top-level comments with expandable reply threads; organizer moderation via overflow menu.
 */
public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.VH> {

    public interface CommentRowListener {
        void onReply(@NonNull EventComment parentComment);

        /** User chose Delete from the overflow menu; activity should confirm then delete. */
        void onDeleteRequested(@NonNull EventComment comment);
    }

    private final CommentRowListener rowListener;
    private final List<EventComment> roots = new ArrayList<>();
    private final Map<String, List<EventComment>> repliesByParent = new HashMap<>();
    private final Set<String> expandedThreadIds = new HashSet<>();

    @Nullable
    private String organizerUserId;
    private boolean canModerate;

    public EventCommentAdapter(@NonNull CommentRowListener rowListener) {
        this.rowListener = rowListener;
    }

    /**
     * @param organizerUserId event organizer id (for Creator badge)
     * @param canModerate     true if current user may delete any comment (organizer)
     */
    public void setOrganizerContext(@Nullable String organizerUserId, boolean canModerate) {
        this.organizerUserId = organizerUserId;
        this.canModerate = canModerate;
        notifyDataSetChanged();
    }

    public void submit(@Nullable List<EventComment> flat) {
        roots.clear();
        repliesByParent.clear();
        if (flat == null) {
            notifyDataSetChanged();
            return;
        }
        for (EventComment c : flat) {
            String pid = c.getParentCommentId();
            if (pid == null || pid.isEmpty()) {
                roots.add(c);
            } else {
                repliesByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(c);
            }
        }
        for (List<EventComment> list : repliesByParent.values()) {
            list.sort(Comparator.comparingLong(EventComment::getCreatedAt));
        }
        notifyDataSetChanged();
    }

    private boolean isCreator(@Nullable EventComment c) {
        if (c == null || organizerUserId == null || c.getUserId() == null) return false;
        return organizerUserId.equals(c.getUserId());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EventComment root = roots.get(position);
        h.bind(root, this, rowListener);
    }

    @Override
    public int getItemCount() {
        return roots.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        private final TextView avatarLetter;
        private final TextView author;
        private final TextView creatorBadge;
        private final TextView body;
        private final TextView time;
        private final TextView reply;
        private final TextView viewReplies;
        private final ImageButton overflow;
        private final LinearLayout repliesContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            avatarLetter = itemView.findViewById(R.id.comment_avatar_letter);
            author = itemView.findViewById(R.id.comment_author);
            creatorBadge = itemView.findViewById(R.id.comment_creator_badge);
            body = itemView.findViewById(R.id.comment_body);
            time = itemView.findViewById(R.id.comment_time);
            reply = itemView.findViewById(R.id.comment_reply);
            viewReplies = itemView.findViewById(R.id.comment_view_replies);
            overflow = itemView.findViewById(R.id.comment_overflow);
            repliesContainer = itemView.findViewById(R.id.comment_replies_container);
        }

        void bind(EventComment root, EventCommentAdapter adapter, CommentRowListener listener) {
            String name = root.getAuthorName() != null ? root.getAuthorName() : itemView.getContext().getString(R.string.placeholder);
            author.setText(name);
            body.setText(root.getBody() != null ? root.getBody() : "");
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.getDefault());
            avatarLetter.setText(initial);

            creatorBadge.setVisibility(adapter.isCreator(root) ? View.VISIBLE : View.GONE);

            if (root.getCreatedAt() > 0) {
                time.setText(new SimpleDateFormat("M-d", Locale.getDefault()).format(new Date(root.getCreatedAt())));
            } else {
                time.setText("");
            }

            reply.setOnClickListener(v -> listener.onReply(root));

            if (adapter.canModerate) {
                overflow.setVisibility(View.VISIBLE);
                overflow.setOnClickListener(v -> {
                    PopupMenu menu = new PopupMenu(itemView.getContext(), overflow);
                    menu.getMenu().add(0, 0, 0, R.string.delete_comment);
                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 0) {
                            listener.onDeleteRequested(root);
                            return true;
                        }
                        return false;
                    });
                    menu.show();
                });
            } else {
                overflow.setVisibility(View.GONE);
                overflow.setOnClickListener(null);
            }

            String id = root.getId();
            List<EventComment> replies = id != null ? adapter.repliesByParent.getOrDefault(id, new ArrayList<>()) : new ArrayList<>();

            if (replies.isEmpty()) {
                viewReplies.setVisibility(View.GONE);
                repliesContainer.setVisibility(View.GONE);
                viewReplies.setOnClickListener(null);
            } else {
                viewReplies.setVisibility(View.VISIBLE);
                boolean expanded = id != null && adapter.expandedThreadIds.contains(id);
                String label;
                if (expanded) {
                    label = itemView.getContext().getString(R.string.hide_replies);
                } else if (replies.size() == 1) {
                    label = itemView.getContext().getString(R.string.view_one_reply);
                } else {
                    label = itemView.getContext().getString(R.string.view_n_replies, replies.size());
                }
                viewReplies.setText(label);

                viewReplies.setOnClickListener(v -> {
                    if (id == null) return;
                    if (adapter.expandedThreadIds.contains(id)) {
                        adapter.expandedThreadIds.remove(id);
                    } else {
                        adapter.expandedThreadIds.add(id);
                    }
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        adapter.notifyItemChanged(pos);
                    }
                });

                repliesContainer.removeAllViews();
                if (expanded) {
                    repliesContainer.setVisibility(View.VISIBLE);
                    LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                    for (EventComment r : replies) {
                        View row = inflater.inflate(R.layout.item_event_comment_reply, repliesContainer, false);
                        TextView ra = row.findViewById(R.id.reply_avatar_letter);
                        TextView rn = row.findViewById(R.id.reply_author);
                        TextView rb = row.findViewById(R.id.reply_body);
                        TextView creator = row.findViewById(R.id.reply_creator_badge);
                        String rnStr = r.getAuthorName() != null ? r.getAuthorName() : "?";
                        rn.setText(rnStr);
                        rb.setText(r.getBody() != null ? r.getBody() : "");
                        String ri = rnStr.isEmpty() ? "?" : rnStr.substring(0, 1).toUpperCase(Locale.getDefault());
                        ra.setText(ri);
                        creator.setVisibility(adapter.isCreator(r) ? View.VISIBLE : View.GONE);
                        repliesContainer.addView(row);
                    }
                } else {
                    repliesContainer.setVisibility(View.GONE);
                }
            }
        }
    }
}
