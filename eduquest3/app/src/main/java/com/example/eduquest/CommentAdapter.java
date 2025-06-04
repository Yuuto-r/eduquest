package com.example.eduquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;
    private final Context context;

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.nameText.setText(comment.getUserName());
        holder.contentText.setText(comment.getContent());

        if (comment.getTimestamp() != null) {
            Date date = comment.getTimestamp().toDate();
            String formattedDate = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(date);
            holder.timestampText.setText(formattedDate);
        } else {
            holder.timestampText.setText("");
        }

        if (comment.getUserProfileUrl() != null && !comment.getUserProfileUrl().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserProfileUrl())
                    .placeholder(R.drawable.account_circle)
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.account_circle);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, contentText, timestampText;
        ImageView profileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.commenterName);
            contentText = itemView.findViewById(R.id.commentContent);
            timestampText = itemView.findViewById(R.id.commentTimestamp);
            profileImage = itemView.findViewById(R.id.commenterProfileImage);
        }
    }
}
