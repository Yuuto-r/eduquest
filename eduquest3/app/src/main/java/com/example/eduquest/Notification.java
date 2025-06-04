package com.example.eduquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notification extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<UserNotification> notificationList;
    private TextView emptyNotificationText;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notification, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        emptyNotificationText = view.findViewById(R.id.emptyNotificationText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            showEmptyMessage(true);
            return;
        }

        db.collection("notifications")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            showEmptyMessage(true);
                            return;
                        }

                        if (value != null) {
                            notificationList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                UserNotification notification = doc.toObject(UserNotification.class);
                                if (notification != null) {
                                    notificationList.add(notification);
                                }
                            }
                            adapter.notifyDataSetChanged();

                            showEmptyMessage(notificationList.isEmpty());
                        }
                    }
                });
    }

    private void showEmptyMessage(boolean show) {
        if (show) {
            emptyNotificationText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyNotificationText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
