package com.example.eduquest;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsDialogFragment extends DialogFragment {

    private static final String ARG_POST_ID = "post_id";

    private String postId;
    private RecyclerView recyclerView;
    private EditText commentInput;
    private Button sendButton;
    private CommentAdapter adapter;
    private List<Comment> commentList = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration commentListener;

    public static CommentsDialogFragment newInstance(String postId) {
        CommentsDialogFragment fragment = new CommentsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_comments, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString(ARG_POST_ID);
        }

        recyclerView = view.findViewById(R.id.recyclerViewComments);
        commentInput = view.findViewById(R.id.commentInput);
        sendButton = view.findViewById(R.id.buttonSendComment);

        db = FirebaseFirestore.getInstance();

        adapter = new CommentAdapter(commentList, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadCommentsRealtime();

        sendButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(getContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "You must be logged in to comment", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            String userName = user.getDisplayName() != null ? user.getDisplayName() : "User";
            String userProfileUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

            CollectionReference commentsRef = db.collection("post")
                    .document(postId)
                    .collection("comments");

            String commentId = commentsRef.document().getId();

            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("commentId", commentId);
            commentMap.put("userId", userId);
            commentMap.put("userName", userName);
            commentMap.put("userProfileUrl", userProfileUrl);
            commentMap.put("content", content);
            commentMap.put("timestamp", Timestamp.now());

            commentsRef.document(commentId)
                    .set(commentMap)
                    .addOnSuccessListener(unused -> commentInput.setText(""))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to post comment", Toast.LENGTH_SHORT).show());
        });

        return view;
    }

    private void loadCommentsRealtime() {
        commentListener = db.collection("post")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        commentList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                commentList.add(comment);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(commentList.size() - 1);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentListener != null) {
            commentListener.remove();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.edit_text_corner_up);
        }
    }
}
