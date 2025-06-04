package com.example.eduquest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AccountInfoActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button changePhotoButton;
    private TextView emailTextView, displayNameTextView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        profileImageView = findViewById(R.id.profileImageView);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        emailTextView = findViewById(R.id.emailTextView);
        displayNameTextView = findViewById(R.id.displayNameTextView);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

        if (currentUser != null) {
            emailTextView.setText("Email: " + currentUser.getEmail());
            displayNameTextView.setText("Name: " + currentUser.getDisplayName());

            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.account_circle)
                        .into(profileImageView);
            }
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadProfileImage(uri);
                    }
                });

        changePhotoButton.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null) return;

        StorageReference fileRef = storageReference.child(currentUser.getUid() + ".jpg");

        UploadTask uploadTask = fileRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();

                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountInfoActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                            Glide.with(AccountInfoActivity.this)
                                    .load(uri)
                                    .placeholder(R.drawable.account_circle)
                                    .into(profileImageView);
                        } else {
                            Toast.makeText(AccountInfoActivity.this, "Failed to update profile photo", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
        ).addOnFailureListener(e ->
                Toast.makeText(AccountInfoActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
