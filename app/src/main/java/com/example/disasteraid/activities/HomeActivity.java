
package com.example.disasteraid.activities;

import android.net.Uri;
import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.disasteraid.R;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.example.disasteraid.helpers.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity
        implements SensorEventListener {

    private TextView tvWelcome;
    private TextView tvRequests;
    private TextView tvVolunteers;
    private TextView tvResources;

    private Button btnCreateRequest;
    private Button btnVolunteers;
    private Button btnResources;
    private Button btnEmergencyCall;
    private Button btnShareEmergency;
    private Button btnFindHospital;


    // Bottom navigation
    private View navHome;
    private View navRequests;
    private View navResources;
    private View navProfile;

    private DatabaseReference database;
    private PreferenceManager preferenceManager;

    private ValueEventListener requestListener;
    private ValueEventListener volunteerListener;
    private ValueEventListener resourceListener;

    // Accelerometer variables
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final long SHAKE_COOLDOWN = 3000;

    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();

        preferenceManager = new PreferenceManager(this);

        String savedName = preferenceManager.getUserName();

        if (!savedName.isEmpty()) {
            tvWelcome.setText("Hello, " + savedName + "!");
        }

        database = FirebaseHelper.getDatabase();

        loadUserName();
        loadStatistics();

        setDashboardListeners();
        setBottomNavigationListeners();
        initializeSensor();
    }

    // Initialize all dashboard views
    private void initializeViews() {

        tvWelcome = findViewById(R.id.tvWelcome);
        tvRequests = findViewById(R.id.tvRequests);
        tvVolunteers = findViewById(R.id.tvVolunteers);
        tvResources = findViewById(R.id.tvResources);

        btnCreateRequest = findViewById(R.id.btnCreateRequest);
        btnVolunteers = findViewById(R.id.btnVolunteers);
        btnResources = findViewById(R.id.btnResources);
        btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        btnShareEmergency = findViewById(R.id.btnShareEmergency);
        btnFindHospital = findViewById(R.id.btnFindHospital);


        // Bottom navigation views
        navHome = findViewById(R.id.navHome);
        navRequests = findViewById(R.id.navRequests);
        navResources = findViewById(R.id.navResources);
        navProfile = findViewById(R.id.navProfile);
    }

    // Existing dashboard button actions
    private void setDashboardListeners() {

        btnCreateRequest.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    RequestListActivity.class
            ));
        });

        btnVolunteers.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    VolunteerListActivity.class
            ));
        });

        btnResources.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    ResourceListActivity.class
            ));
        });

        // Image picker
        View btnImagePicker = findViewById(R.id.btnImagePicker);

        if (btnImagePicker != null) {
            btnImagePicker.setOnClickListener(v -> {
                startActivity(new Intent(
                        HomeActivity.this,
                        ImagePickerActivity.class
                ));
            });
        }

        // Emergency call
        btnEmergencyCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:999")
            );

            startActivity(callIntent);
        });

        // Share emergency information
        btnShareEmergency.setOnClickListener(v -> {

            String message =
                    "Emergency! I need assistance. " +
                            "Please contact me as soon as possible.";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(
                    shareIntent,
                    "Share Emergency Information"
            ));
        });

        // Find nearby hospitals
        btnFindHospital.setOnClickListener(v -> {

            Uri mapUri = Uri.parse(
                    "geo:0,0?q=nearby hospitals"
            );

            Intent mapIntent = new Intent(
                    Intent.ACTION_VIEW,
                    mapUri
            );

            startActivity(mapIntent);
        });


    }

    // Bottom navigation actions
    private void setBottomNavigationListeners() {

        // Home tab
        navHome.setOnClickListener(v -> {
            // Already on Home screen
        });

        // Requests tab
        navRequests.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    RequestListActivity.class
            ));
        });

        // Resources tab
        navResources.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    ResourceListActivity.class
            ));
        });

        // Profile tab
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            ));
        });
    }

    // Initialize accelerometer
    private void initializeSensor() {

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
            );
        }
    }

    // Load username from Firebase and save locally
    private void loadUserName() {

        String uid = FirebaseHelper.getCurrentUserId();

        if (uid == null) {
            openLoginScreen();
            return;
        }

        database.child("users")
                .child(uid)
                .child("fullName")
                .get()
                .addOnSuccessListener(snapshot -> {

                    String fullName = snapshot.getValue(String.class);

                    if (fullName != null &&
                            !fullName.trim().isEmpty()) {

                        preferenceManager.saveUserName(fullName);

                        tvWelcome.setText(
                                "Hello, " + fullName + "!"
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                HomeActivity.this,
                                "Unable to load your profile name",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // Load dashboard statistics
    private void loadStatistics() {

        requestListener = createCountListener(tvRequests);
        volunteerListener = createCountListener(tvVolunteers);
        resourceListener = createCountListener(tvResources);

        database.child("aidRequests")
                .addValueEventListener(requestListener);

        database.child("volunteers")
                .addValueEventListener(volunteerListener);

        database.child("resources")
                .addValueEventListener(resourceListener);
    }

    // Count Firebase records
    private ValueEventListener createCountListener(TextView textView) {

        return new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                long count = snapshot.getChildrenCount();

                textView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError error) {

                textView.setText("--");

                Toast.makeText(
                        HomeActivity.this,
                        "Unable to load dashboard statistics",
                        Toast.LENGTH_SHORT
                ).show();
            }
        };
    }

    // Accelerometer lifecycle: start listening
    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null && accelerometer != null) {

            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    // Stop listening when activity is paused
    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // Detect phone shaking
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(
                    x * x + y * y + z * z
            );

            long currentTime = System.currentTimeMillis();

            if (acceleration > SHAKE_THRESHOLD &&
                    currentTime - lastShakeTime > SHAKE_COOLDOWN) {

                lastShakeTime = currentTime;

                showEmergencyConfirmation();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No action required
    }

    // Show emergency confirmation
    private void showEmergencyConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage(
                        "You shook your phone. Do you want to create an emergency request?"
                )
                .setPositiveButton("Yes", (dialog, which) -> {

                    Intent intent = new Intent(
                            HomeActivity.this,
                            RequestListActivity.class
                    );

                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }



    // Open login screen
    private void openLoginScreen() {

        Intent intent = new Intent(
                this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // Remove Firebase listeners
    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (database != null) {

            if (requestListener != null) {
                database.child("aidRequests")
                        .removeEventListener(requestListener);
            }

            if (volunteerListener != null) {
                database.child("volunteers")
                        .removeEventListener(volunteerListener);
            }

            if (resourceListener != null) {
                database.child("resources")
                        .removeEventListener(resourceListener);
            }
        }
    }
}
