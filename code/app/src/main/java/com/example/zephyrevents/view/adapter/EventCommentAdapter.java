package com.example.zephyrevents.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zephyrevents.R;
import com.example.zephyrevents.model.EventComment;
import com.example.zephyrevents.model.User;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.UserRepository;

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
 * Top-level comments with expandable reply threads. Organizers/admins may delete any comment;
 * other users may delete only their own. Host badge = primary organizer or co-organizer for this event.
 */
public class EventCommentAdapter extends RecyclerView.Adapter<EventCommentAdapter.VH> {

    public interface CommentRowListener {
        void onReply(@NonNull EventComment parentComment);

        void onDeleteRequested(@NonNull EventComment comment);
    }

    private final CommentRowListener rowListener;
    private final List<EventComment> roots = new ArrayList<>();
    private final Map<String, List<EventComment>> repliesByParent = new HashMap<>();
    private final Set<String> expandedThreadIds = new HashSet<>();

    private final Set<String> eventHostUserIds = new HashSet<>();
    private boolean canModerate;
    @Nullable
    private String currentUserId;

    private final UserRepository userRepository = new UserRepository();
    /** userId → resolved avatar URL (empty string = no photo). */
    private final Map<String, String> resolvedAvatarByUserId = new HashMap<>();
    private final Set<String> avatarLoadInFlight = new HashSet<>();

    public EventCommentAdapter(@NonNull CommentRowListener rowListener) {
        this.rowListener = rowListener;
    }

    /**
     * @param organizerUserId     primary organizer (creator badge + profile consistency)
     * @param coOrganizerUserIds  co-organizers for this event (also get creator badge)
     * @param canModerate         organizer/co-org/admin may delete any comment
     * @param currentUserId       signed-in user id for delete-own
     */
    public void setOrganizerContext(@Nullable String organizerUserId, @Nullable List<String> coOrganizerUserIds,
                                    boolean canModerate, @Nullable String currentUserId) {
        eventHostUserIds.clear();
        if (organizerUserId != null) {
            eventHostUserIds.add(organizerUserId);
        }
        if (coOrganizerUserIds != null) {
            for (String id : coOrganizerUserIds) {
                if (id != null) {
                    eventHostUserIds.add(id);
                }
            }
        }
        this.canModerate = canModerate;
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    private boolean canShowDeleteFor(@Nullable EventComment c) {
        if (c == null) return false;
        if (canModerate) return true;
        if (currentUserId == null || c.getUserId() == null) return false;
        return currentUserId.equals(c.getUserId());
    }

    private boolean isHostComment(@Nullable EventComment c) {
        return c != null && c.getUserId() != null && eventHostUserIds.contains(c.getUserId());
    }

    private static void bindOverflowMenu(@NonNull ImageButton overflow, @NonNull EventComment comment,
                                         @NonNull EventCommentAdapter adapter, @NonNull CommentRowListener listener) {
        if (adapter.canShowDeleteFor(comment)) {
            overflow.setVisibility(View.VISIBLE);
            overflow.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(overflow.getContext(), overflow);
                menu.getMenu().add(0, 0, 0, R.string.delete_comment);
                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        listener.onDeleteRequested(comment);
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
    }

    private void bindAvatar(@NonNull ImageView imageView, @NonNull TextView letterView,
                            @NonNull EventComment comment, int rootRowToRefreshOnResolve) {
        String name = comment.getAuthorName() != null ? comment.getAuthorName() : letterView.getContext().getString(R.string.placeholder);
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.getDefault());
        letterView.setText(initial);

        String fromDoc = comment.getAuthorAvatarUrl();
        if (!TextUtils.isEmpty(fromDoc)) {
            letterView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(imageView)
                    .load(fromDoc.trim())
                    .circleCrop()
                    .placeholder(R.drawable.bg_comment_avatar)
                    .error(R.drawable.bg_comment_avatar)
                    .into(imageView);
            return;
        }

        String uid = comment.getUserId();
        if (uid != null && resolvedAvatarByUserId.containsKey(uid)) {
            String cached = resolvedAvatarByUserId.get(uid);
            if (!TextUtils.isEmpty(cached)) {
                letterView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(imageView)
                        .load(cached)
                        .circleCrop()
                        .placeholder(R.drawable.bg_comment_avatar)
                        .error(R.drawable.bg_comment_avatar)
                        .into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
                letterView.setVisibility(View.VISIBLE);
                Glide.with(imageView).clear(imageView);
            }
            return;
        }

        if (uid != null && avatarLoadInFlight.add(uid)) {
            userRepository.getUserById(uid, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    String url = user != null && user.getAvatarUrl() != null ? user.getAvatarUrl().trim() : "";
                    resolvedAvatarByUserId.put(uid, url);
                    avatarLoadInFlight.remove(uid);
                    if (rootRowToRefreshOnResolve != RecyclerView.NO_POSITION) {
                        EventCommentAdapter.this.notifyItemChanged(rootRowToRefreshOnResolve);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    resolvedAvatarByUserId.put(uid, "");
                    avatarLoadInFlight.remove(uid);
                    if (rootRowToRefreshOnResolve != RecyclerView.NO_POSITION) {
                        EventCommentAdapter.this.notifyItemChanged(rootRowToRefreshOnResolve);
                    }
                }
            });
        }

        imageView.setVisibility(View.GONE);
        letterView.setVisibility(View.VISIBLE);
        Glide.with(imageView).clear(imageView);
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
        private final ImageView avatarImage;
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
            avatarImage = itemView.findViewById(R.id.comment_avatar_image);
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

            int pos = getBindingAdapterPosition();
            adapter.bindAvatar(avatarImage, avatarLetter, root, pos);

            creatorBadge.setVisibility(adapter.isHostComment(root) ? View.VISIBLE : View.GONE);

            if (root.getCreatedAt() > 0) {
                time.setText(new SimpleDateFormat("M-d", Locale.getDefault()).format(new Date(root.getCreatedAt())));
            } else {
                time.setText("");
            }

            reply.setOnClickListener(v -> listener.onReply(root));

            bindOverflowMenu(overflow, root, adapter, listener);

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
                    int p = getBindingAdapterPosition();
                    if (p != RecyclerView.NO_POSITION) {
                        adapter.notifyItemChanged(p);
                    }
                });

                repliesContainer.removeAllViews();
                if (expanded) {
                    repliesContainer.setVisibility(View.VISIBLE);
                    LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                    int rootPos = getBindingAdapterPosition();
                    for (EventComment r : replies) {
                        View row = inflater.inflate(R.layout.item_event_comment_reply, repliesContainer, false);
                        ImageView rImg = row.findViewById(R.id.reply_avatar_image);
                        TextView ra = row.findViewById(R.id.reply_avatar_letter);
                        TextView rn = row.findViewById(R.id.reply_author);
                        TextView rb = row.findViewById(R.id.reply_body);
                        TextView creator = row.findViewById(R.id.reply_creator_badge);
                        ImageButton replyOverflow = row.findViewById(R.id.reply_overflow);
                        String rnStr = r.getAuthorName() != null ? r.getAuthorName() : "?";
                        rn.setText(rnStr);
                        rb.setText(r.getBody() != null ? r.getBody() : "");
                        adapter.bindAvatar(rImg, ra, r, rootPos);
                        creator.setVisibility(adapter.isHostComment(r) ? View.VISIBLE : View.GONE);
                        bindOverflowMenu(replyOverflow, r, adapter, listener);
                        repliesContainer.addView(row);
                    }
                } else {
                    repliesContainer.setVisibility(View.GONE);
                }
            }
        }
    }
}
