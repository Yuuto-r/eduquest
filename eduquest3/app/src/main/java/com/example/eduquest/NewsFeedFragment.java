package com.example.eduquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFeedFragment extends Fragment {

    private EditText postInput;
    private Button btnPost;
    private RecyclerView recyclerView;
    private ImageView profileImage;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_news_feed_fragment, container, false);

        postInput = view.findViewById(R.id.postInput);
        btnPost = view.findViewById(R.id.btnPost);
        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        profileImage = view.findViewById(R.id.profileImageInput);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(requireContext())
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.drawable.account_circle)
                    .into(profileImage);
        }

        adapter = new PostAdapter(postList, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        FirebaseFirestore.getInstance()
                .collection("post")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String postId = doc.getString("postId");
                        String userName = doc.getString("userName");
                        String userProfileUrl = doc.getString("userProfileUrl");
                        String content = doc.getString("content");
                        String imageUrl = doc.getString("imageUrl");
                        Timestamp ts = doc.getTimestamp("timestamp");

                        Map<String, String> reactions = (Map<String, String>) doc.get("reactions");

                        if (postId == null || postId.isEmpty() ||
                                userName == null || userName.isEmpty() ||
                                content == null || content.isEmpty()) {
                            continue;
                        }

                        Post post = new Post(postId, userName, userProfileUrl, content, imageUrl);
                        post.setTimestamp(ts);
                        post.setReactions(reactions != null ? reactions : new HashMap<>());

                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                });

        btnPost.setOnClickListener(v -> {
            String content = postInput.getText().toString().trim();

            if (!content.isEmpty()) {
                if (user == null) {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
                String postId = FirebaseFirestore.getInstance().collection("post").document().getId();

                Map<String, Object> postMap = new HashMap<>();
                postMap.put("postId", postId);
                postMap.put("userName", name);
                postMap.put("userProfileUrl", photoUrl);
                postMap.put("content", content);
                postMap.put("imageUrl", "");
                postMap.put("timestamp", FieldValue.serverTimestamp());
                postMap.put("reactions", new HashMap<String, String>());

                FirebaseFirestore.getInstance().collection("post")
                        .document(postId)
                        .set(postMap)
                        .addOnSuccessListener(unused -> {
                            Post newPost = new Post(postId, name, photoUrl, content, "");
                            newPost.setReactions(new HashMap<>());
                            postList.add(0, newPost);
                            adapter.notifyItemInserted(0);
                            recyclerView.scrollToPosition(0);
                            postInput.setText("");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to post", Toast.LENGTH_SHORT).show()
                        );
            }
        });

        return view;
    }
}
