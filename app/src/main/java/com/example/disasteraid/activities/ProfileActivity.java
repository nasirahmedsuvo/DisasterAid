
package com.example.disasteraid.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.disasteraid.R;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.example.disasteraid.helpers.PreferenceManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileRole;
    private TextView tvProfileFullName;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileRoleDetail;

    private Button btnEditProfileDetails;
    private Button btnProfileSettings;
    private Button btnProfileLogout;

    private DatabaseReference userReference;
    private ValueEventListener profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        initializeFirebase();

        if (userReference == null) {
            return;
        }

        loadProfile();

        btnEditProfileDetails.setOnClickListener(v ->
                showEditProfileDialog()
        );

        btnProfileSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ProfileActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        btnProfileLogout.setOnClickListener(v -> {
            FirebaseHelper.getAuth().signOut();
            new PreferenceManager(this).clearUserName();
            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileRoleDetail = findViewById(R.id.tvProfileRoleDetail);

        btnEditProfileDetails =
                findViewById(R.id.btnEditProfileDetails);

        btnProfileSettings =
                findViewById(R.id.btnProfileSettings);

        btnProfileLogout =
                findViewById(R.id.btnProfileLogout);
    }

    private void initializeFirebase() {

        String uid = FirebaseHelper.getCurrentUserId();

        if (uid == null) {
            Toast.makeText(
                    this,
                    "Please login to view your profile",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // getDatabase() already returns a DatabaseReference.
        userReference = FirebaseHelper.getDatabase()
                .child("users")
                .child(uid);
    }

    private void loadProfile() {

        if (userReference == null) {
            return;
        }

        FirebaseUser currentUser =
                FirebaseHelper.getAuth().getCurrentUser();

        if (currentUser != null && currentUser.getEmail() != null) {
            tvProfileEmail.setText(currentUser.getEmail());
        }

        profileListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String name = snapshot.child("fullName")
                        .getValue(String.class);

                String phone = snapshot.child("phone")
                        .getValue(String.class);

                String role = snapshot.child("role")
                        .getValue(String.class);

                if (name == null || name.trim().isEmpty()) {
                    name = "Not provided";
                }

                if (phone == null || phone.trim().isEmpty()) {
                    phone = "Not provided";
                }

                if (role == null || role.trim().isEmpty()) {
                    role = "Member";
                }

                tvProfileName.setText(name);
                tvProfileFullName.setText(name);
                tvProfilePhone.setText(phone);

                tvProfileRole.setText(role);
                tvProfileRoleDetail.setText(role);
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(
                        ProfileActivity.this,
                        "Unable to load profile: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        userReference.addValueEventListener(profileListener);
    }

    private void showEditProfileDialog() {

        if (userReference == null) {
            return;
        }

        EditText etName = new EditText(this);
        etName.setHint("Full name");
        etName.setSingleLine(true);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone number");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhone.setSingleLine(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);

        layout.setPadding(padding, 0, padding, 0);

        layout.addView(etName);
        layout.addView(etPhone);

        userReference.get().addOnSuccessListener(snapshot -> {

            String currentName = snapshot.child("fullName")
                    .getValue(String.class);

            String currentPhone = snapshot.child("phone")
                    .getValue(String.class);

            etName.setText(currentName == null ? "" : currentName);
            etPhone.setText(currentPhone == null ? "" : currentPhone);

            new AlertDialog.Builder(this)
                    .setTitle("Edit Your Profile")
                    .setView(layout)
                    .setPositiveButton("Save", (dialog, which) -> {

                        String name = etName.getText()
                                .toString().trim();

                        String phone = etPhone.getText()
                                .toString().trim();

                        if (name.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "Please enter your name",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (!phone.isEmpty() &&
                                !phone.matches("[0-9+() -]{7,20}")) {

                            Toast.makeText(
                                    this,
                                    "Please enter a valid phone number",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("fullName", name);
                        updates.put("phone", phone);

                        userReference.updateChildren(updates)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(
                                                this,
                                                "Profile updated successfully",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                )
                                .addOnFailureListener(error ->
                                        Toast.makeText(
                                                this,
                                                "Update failed: " +
                                                        error.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        }).addOnFailureListener(error -> {

            Toast.makeText(
                    this,
                    "Unable to open profile editor: " +
                            error.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (userReference != null && profileListener != null) {
            userReference.removeEventListener(profileListener);
        }
    }
}
