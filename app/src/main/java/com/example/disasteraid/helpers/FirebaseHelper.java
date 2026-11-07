package com.example.disasteraid.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private static DatabaseReference database =
            FirebaseDatabase.getInstance().getReference();

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static DatabaseReference getDatabase() {
        return database;
    }

    public static String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }
}