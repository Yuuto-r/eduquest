package com.example.eduquest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final Context context;
    private final FirebaseFirestore db;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.nameText.setText(post.getUserName());
        holder.contentText.setText(post.getContent());

        if (post.getUserProfileUrl() != null && !post.getUserProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getUserProfileUrl())
                    .placeholder(R.drawable.account_circle)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.account_circle);
        }

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.account_circle)
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, String> reactions = post.getReactions();
        boolean isLiked = reactions != null && reactions.containsKey(currentUserId);

        holder.likeButton.setImageResource(isLiked ? R.drawable.ic_like_button : R.drawable.ic_like_outline);
        holder.likeCountText.setText((reactions != null ? reactions.size() : 0) + " likes");

        holder.likeButton.setOnClickListener(v -> {
            String postId = post.getPostId();

            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Invalid post ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> updatedReactions = reactions != null ? new HashMap<>(reactions) : new HashMap<>();

            boolean isCurrentlyLiked = updatedReactions.containsKey(currentUserId);

            if (isCurrentlyLiked) {
                updatedReactions.remove(currentUserId);
            } else {
                updatedReactions.put(currentUserId, "like");
            }

            db.collection("post").document(postId)
                    .update("reactions", updatedReactions)
                    .addOnSuccessListener(aVoid -> {
                        post.setReactions(updatedReactions);
                        notifyItemChanged(position);

                        if (!isCurrentlyLiked) {
                            addLikeNotification(post, currentUserId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show();
                        Log.e("PostAdapter", "Error updating like", e);
                    });
        });

        holder.commentButton.setOnClickListener(v -> {
            String postId = post.getPostId();
            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Invalid post ID for comments", Toast.LENGTH_SHORT).show();
                return;
            }

            if (context instanceof FragmentActivity) {
                CommentsDialogFragment commentsDialog = CommentsDialogFragment.newInstance(postId);
                commentsDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "comments_dialog");
            } else {
                Toast.makeText(context, "Unable to open comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLikeNotification(Post post, String currentUserId) {
        String postOwnerId = post.getUserId();
        if (postOwnerId == null || postOwnerId.equals(currentUserId)) {
            return;
        }

        String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (senderName == null) senderName = "Someone";

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "like");
        notification.put("senderId", currentUserId);
        notification.put("senderName", senderName);
        notification.put("receiverId", postOwnerId);
        notification.put("postId", post.getPostId());
        notification.put("message", senderName + " liked your post");
        notification.put("timestamp", com.google.firebase.Timestamp.now());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    Log.d("PostAdapter", "Like notification added");
                })
                .addOnFailureListener(e -> {
                    Log.e("PostAdapter", "Failed to add like notification", e);
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, contentText, likeCountText;
        ImageView profileImage, postImage;
        ImageButton likeButton;
        Button commentButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            contentText = itemView.findViewById(R.id.contentText);
            profileImage = itemView.findViewById(R.id.profileImage);
            postImage = itemView.findViewById(R.id.postImage);
            likeButton = itemView.findViewById(R.id.likebutton);
            likeCountText = itemView.findViewById(R.id.reactCount);
            commentButton = itemView.findViewById(R.id.commentButton);
        }
    }
}
