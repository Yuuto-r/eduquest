package com.example.eduquest;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class Socialize extends AppCompatActivity {

    ImageButton btnProfile, btnNotifications, btnBack, btnReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socialize);


        btnProfile = findViewById(R.id.btnProfile);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnBack = findViewById(R.id.btnBack);
        btnReload = findViewById(R.id.btnReload);


        loadFragment(new NewsFeedFragment());


        btnProfile.setOnClickListener(v -> loadFragment(new NewsFeedFragment()));
        btnNotifications.setOnClickListener(v -> loadFragment(new Notification()));

        btnBack.setOnClickListener(v -> onBackPressed());

        btnReload.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (currentFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .detach(currentFragment)
                        .attach(currentFragment)
                        .commit();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
