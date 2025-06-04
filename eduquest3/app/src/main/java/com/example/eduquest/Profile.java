package com.example.eduquest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView usernameTextView;
    private ImageView profileImage;
    private Button changePasswordBtn, accountBtn, termsBtn, logoutBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private String userId;
    private boolean isGoogleSignIn = false;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameTextView = findViewById(R.id.usernameTextView);
        profileImage = findViewById(R.id.profileImage);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        accountBtn = findViewById(R.id.accountBtn);
        termsBtn = findViewById(R.id.termsBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();

            for (UserInfo userInfo : currentUser.getProviderData()) {
                if ("google.com".equals(userInfo.getProviderId())) {
                    isGoogleSignIn = true;
                    break;
                }
            }

            loadUserProfile();
        }

        profileImage.setOnClickListener(v -> {
            if (!isGoogleSignIn) {
                chooseImage();
            } else {
                Toast.makeText(this, "Google account image can't be changed here", Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordBtn.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        accountBtn.setOnClickListener(v -> startActivity(new Intent(this, AccountInfoActivity.class)));

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(Profile.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        if (isGoogleSignIn) {
            usernameTextView.setText(currentUser.getDisplayName());
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.account_circle)
                    .into(profileImage);
        } else {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.getString("name");
                            String imageUrl = snapshot.getString("profileImage");

                            usernameTextView.setText(name != null ? name : "User");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.account_circle)
                                        .into(profileImage);
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            StorageReference fileRef = storageReference.child(userId + ".jpg");

            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();

                                db.collection("users").document(userId)
                                        .update("profileImage", downloadUrl)
                                        .addOnSuccessListener(aVoid -> {
                                            Glide.with(Profile.this).load(downloadUrl).into(profileImage);
                                            Toast.makeText(Profile.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(Profile.this, "Failed to update profile image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            })
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(Profile.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadUserProfile();
        }
    }
}
