package com.example.eduquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<UserNotification> notificationList;

    public NotificationAdapter(List<UserNotification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserNotification notification = notificationList.get(position);

        holder.messageText.setText(notification.getMessage());

        if (notification.getSenderName() != null && !notification.getSenderName().isEmpty()) {
            holder.senderNameText.setText(notification.getSenderName());
        } else {
            holder.senderNameText.setText("Unknown");
        }

        Timestamp timestamp = notification.getTimestamp();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.timestampText.setText(sdf.format(date));
        } else {
            holder.timestampText.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView senderNameText;
        TextView timestampText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.notificationMessage);
            senderNameText = itemView.findViewById(R.id.notificationSenderName);
            timestampText = itemView.findViewById(R.id.notificationTimestamp);
        }
    }
}
