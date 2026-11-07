package com.example.disasteraid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.disasteraid.activities.HomeActivity;
import com.example.disasteraid.activities.LoginActivity;
import com.example.disasteraid.helpers.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = new PreferenceManager(this);
        if (preferenceManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            startActivity(new Intent(this, HomeActivity.class));

        } else {

            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
