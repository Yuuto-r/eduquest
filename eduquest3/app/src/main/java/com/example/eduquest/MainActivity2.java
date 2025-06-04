package com.example.eduquest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recyclerView = findViewById(R.id.recyclerViewPost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(postAdapter);

        loadPostsFromFirestore();
    }

    private void loadPostsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("post")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(doc.getId());
                            postList.add(post);
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity2.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                });
    }
}
