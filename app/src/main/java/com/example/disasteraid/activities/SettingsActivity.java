
package com.example.disasteraid.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.disasteraid.R;
import com.example.disasteraid.helpers.PreferenceManager;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.google.firebase.database.DatabaseReference;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkMode;
    private SwitchCompat switchNotifications;
    private EditText etEmergencyContact;

    private Button btnSaveContact;
    private Button btnEditProfile;
    private Button btnAbout;

    private PreferenceManager preferenceManager;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();

        preferenceManager = new PreferenceManager(this);

        String userId = FirebaseHelper.getCurrentUserId();

        if (userId != null) {
            userReference = FirebaseHelper.getDatabase()
                    .child("users")
                    .child(userId);
        }

        loadSettings();
        setListeners();
    }

    private void initializeViews() {

        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);

        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAbout = findViewById(R.id.btnAbout);
    }

    private void loadSettings() {

        switchDarkMode.setChecked(
                preferenceManager.isDarkModeEnabled()
        );

        switchNotifications.setChecked(
                preferenceManager.areNotificationsEnabled()
        );

        etEmergencyContact.setText(
                preferenceManager.getEmergencyContact()
        );
    }

    private void setListeners() {

        switchDarkMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferenceManager.saveDarkModePreference(isChecked);

                    AppCompatDelegate.setDefaultNightMode(
                            isChecked
                                    ? AppCompatDelegate.MODE_NIGHT_YES
                                    : AppCompatDelegate.MODE_NIGHT_NO
                    );

                    Toast.makeText(
                            this,
                            "Dark mode " + (isChecked ? "enabled" : "disabled"),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferenceManager.saveNotificationPreference(isChecked);

                    Toast.makeText(
                            this,
                            "Notification preference saved",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        btnSaveContact.setOnClickListener(v -> saveEmergencyContact());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void saveEmergencyContact() {

        String phone = etEmergencyContact.getText()
                .toString()
                .trim();

        if (phone.isEmpty()) {
            etEmergencyContact.setError("Enter a phone number");
            etEmergencyContact.requestFocus();
            return;
        }

        if (!phone.matches("[0-9+() -]{7,20}")) {
            etEmergencyContact.setError("Enter a valid phone number");
            etEmergencyContact.requestFocus();
            return;
        }

        preferenceManager.saveEmergencyContact(phone);

        Toast.makeText(
                this,
                "Emergency contact saved",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showEditProfileDialog() {

        if (userReference == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        EditText nameInput = new EditText(this);
        nameInput.setHint("Enter your full name");

        EditText phoneInput = new EditText(this);
        phoneInput.setHint("Enter your phone number");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);

        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL
        );

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);

        layout.setPadding(padding, 0, padding, 0);

        layout.addView(nameInput);
        layout.addView(phoneInput);

        userReference.get().addOnSuccessListener(snapshot -> {

            String oldName = snapshot.child("fullName")
                    .getValue(String.class);

            String oldPhone = snapshot.child("phone")
                    .getValue(String.class);

            nameInput.setText(oldName == null ? "" : oldName);
            phoneInput.setText(oldPhone == null ? "" : oldPhone);

            new AlertDialog.Builder(this)
                    .setTitle("Edit Profile")
                    .setView(layout)
                    .setPositiveButton("Save", (dialog, which) -> {

                        String name = nameInput.getText()
                                .toString().trim();

                        String phone = phoneInput.getText()
                                .toString().trim();

                        if (name.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "Name cannot be empty",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (!phone.isEmpty() &&
                                !phone.matches("[0-9+() -]{7,20}")) {
                            Toast.makeText(
                                    this,
                                    "Enter a valid phone number",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        userReference.child("fullName")
                                .setValue(name);

                        userReference.child("phone")
                                .setValue(phone);

                        preferenceManager.saveUserName(name);

                        Toast.makeText(
                                this,
                                "Profile updated",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        }).addOnFailureListener(error -> {

            Toast.makeText(
                    this,
                    "Unable to load profile",
                    Toast.LENGTH_SHORT
            ).show();

        });
    }

    private void showAboutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("About DisasterAid")
                .setMessage(
                        "DisasterAid v1.0\n\n" +
                                "Emergency Resource & Volunteer Network\n\n" +
                                "This application connects disaster-affected " +
                                "people with volunteers and emergency resources.\n\n" +
                                "Developed as an Android Lab Project."
                )
                .setPositiveButton("OK", null)
                .show();
    }
}
