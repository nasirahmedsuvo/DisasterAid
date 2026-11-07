
package com.example.disasteraid.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.disasteraid.R;
import com.example.disasteraid.adapters.VolunteerAdapter;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.example.disasteraid.models.Volunteer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VolunteerListActivity extends AppCompatActivity {

    private RecyclerView recyclerVolunteers;
    private TextView tvVolunteerEmpty;
    private FloatingActionButton fabAddVolunteer;

    private List<Volunteer> volunteerList;
    private VolunteerAdapter adapter;

    private DatabaseReference volunteersRef;
    private ValueEventListener volunteersListener;

    private String currentUserId;

    private static final int WHITE = Color.WHITE;
    private static final int PRIMARY = Color.rgb(23, 59, 100);
    private static final int SECONDARY = Color.rgb(22, 160, 133);
    private static final int TEXT_DARK = Color.rgb(36, 50, 71);
    private static final int TEXT_HINT = Color.rgb(123, 135, 152);

    private final String[] availabilityOptions = {
            "Available anytime",
            "Morning",
            "Afternoon",
            "Evening",
            "Weekends"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_list);

        currentUserId = FirebaseHelper.getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerVolunteers = findViewById(R.id.recyclerVolunteers);
        tvVolunteerEmpty = findViewById(R.id.tvVolunteerEmpty);
        fabAddVolunteer = findViewById(R.id.fabAddVolunteer);

        volunteersRef = FirebaseHelper.getDatabase()
                .child("volunteers");

        volunteerList = new ArrayList<>();

        adapter = new VolunteerAdapter(
                volunteerList,
                currentUserId,
                new VolunteerAdapter.VolunteerActionListener() {
                    @Override
                    public void onEdit(Volunteer volunteer) {
                        showVolunteerDialog(volunteer);
                    }

                    @Override
                    public void onDelete(Volunteer volunteer) {
                        confirmDelete(volunteer);
                    }
                }
        );

        recyclerVolunteers.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerVolunteers.setAdapter(adapter);

        fabAddVolunteer.setOnClickListener(v ->
                showVolunteerDialog(null));

        loadVolunteers();
    }

    // READ: Load volunteers from Firebase
    private void loadVolunteers() {

        volunteersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                volunteerList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Volunteer volunteer =
                            data.getValue(Volunteer.class);

                    if (volunteer != null) {
                        volunteer.setVolunteerId(data.getKey());
                        volunteerList.add(volunteer);
                    }
                }

                adapter.notifyDataSetChanged();

                tvVolunteerEmpty.setVisibility(
                        volunteerList.isEmpty()
                                ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        VolunteerListActivity.this,
                        "Failed to load volunteers: "
                                + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        volunteersRef.addValueEventListener(volunteersListener);
    }

    // CREATE / UPDATE: Volunteer form
    private void showVolunteerDialog(Volunteer existingVolunteer) {

        boolean isEditing = existingVolunteer != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(40, 20, 40, 10);
        form.setBackgroundColor(WHITE);

        EditText etName = createInput(
                "Full Name", InputType.TYPE_CLASS_TEXT);

        EditText etPhone = createInput(
                "Phone Number", InputType.TYPE_CLASS_PHONE);

        EditText etSkills = createInput(
                "Skills (e.g. First Aid, Driving)",
                InputType.TYPE_CLASS_TEXT);

        Spinner spAvailability = new Spinner(this);

        ArrayAdapter<String> availabilityAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        availabilityOptions
                ) {
                    @NonNull
                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            @NonNull android.view.ViewGroup parent) {

                        TextView text = (TextView) super.getView(
                                position, convertView, parent);
                        text.setTextColor(TEXT_DARK);
                        text.setTextSize(15);
                        return text;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            @NonNull android.view.ViewGroup parent) {

                        TextView text = (TextView) super.getDropDownView(
                                position, convertView, parent);
                        text.setTextColor(TEXT_DARK);
                        text.setBackgroundColor(WHITE);
                        text.setPadding(20, 16, 20, 16);
                        return text;
                    }
                };

        availabilityAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spAvailability.setAdapter(availabilityAdapter);
        spAvailability.setBackgroundTintList(
                ColorStateList.valueOf(SECONDARY));

        EditText etLocation = createInput(
                "Your Location",
                InputType.TYPE_CLASS_TEXT);

        form.addView(etName);
        form.addView(etPhone);
        form.addView(etSkills);
        form.addView(spAvailability);
        form.addView(etLocation);

        if (isEditing) {

            etName.setText(existingVolunteer.getName());
            etPhone.setText(existingVolunteer.getPhone());
            etSkills.setText(existingVolunteer.getSkills());
            etLocation.setText(existingVolunteer.getLocation());

            int selected = availabilityAdapter.getPosition(
                    existingVolunteer.getAvailability());

            if (selected >= 0) {
                spAvailability.setSelection(selected);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing
                        ? "Edit Volunteer Profile"
                        : "Register as Volunteer")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        isEditing ? "Update" : "Register", null)
                .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(WHITE));
            }

            TextView titleView = dialog.findViewById(
                    androidx.appcompat.R.id.alertTitle);

            if (titleView != null) {
                titleView.setTextColor(PRIMARY);
                titleView.setTextSize(20);
            }

            Button cancelButton = dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE);

            if (cancelButton != null) {
                cancelButton.setTextColor(SECONDARY);
            }

            Button saveButton = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE);

            if (saveButton != null) {
                saveButton.setTextColor(SECONDARY);

                saveButton.setOnClickListener(v -> {

                    String name = etName.getText()
                            .toString().trim();

                    String phone = etPhone.getText()
                            .toString().trim();

                    String skills = etSkills.getText()
                            .toString().trim();

                    String availability =
                            spAvailability.getSelectedItem().toString();

                    String location = etLocation.getText()
                            .toString().trim();

                    if (name.isEmpty()) {
                        etName.setError("Enter your name");
                        return;
                    }

                    if (phone.isEmpty()) {
                        etPhone.setError("Enter your phone number");
                        return;
                    }

                    if (!phone.matches("[0-9+() -]{7,20}")) {
                        etPhone.setError("Enter a valid phone number");
                        return;
                    }

                    if (skills.isEmpty()) {
                        etSkills.setError("Enter your skills");
                        return;
                    }

                    if (location.isEmpty()) {
                        etLocation.setError("Enter your location");
                        return;
                    }

                    if (isEditing) {
                        updateVolunteer(
                                existingVolunteer,
                                name, phone, skills,
                                availability, location);
                    } else {
                        createVolunteer(
                                name, phone, skills,
                                availability, location);
                    }

                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    // Reusable input field
    private EditText createInput(String hint, int inputType) {

        EditText editText = new EditText(this);

        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setTextColor(TEXT_DARK);
        editText.setHintTextColor(TEXT_HINT);
        editText.setTextSize(15);

        editText.setBackgroundTintList(
                ColorStateList.valueOf(SECONDARY));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = 12;
        editText.setLayoutParams(params);

        return editText;
    }

    // CREATE: Save volunteer
    private void createVolunteer(
            String name,
            String phone,
            String skills,
            String availability,
            String location) {

        String volunteerId = volunteersRef.push().getKey();

        if (volunteerId == null) {
            Toast.makeText(this, "Could not create volunteer ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Volunteer volunteer = new Volunteer(
                volunteerId,
                currentUserId,
                name,
                phone,
                skills,
                availability,
                location
        );

        volunteersRef.child(volunteerId)
                .setValue(volunteer)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Volunteer registered successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Registration failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // UPDATE: Update own volunteer profile
    private void updateVolunteer(
            Volunteer oldVolunteer,
            String name,
            String phone,
            String skills,
            String availability,
            String location) {

        if (!currentUserId.equals(oldVolunteer.getUserId())) {
            Toast.makeText(this,
                    "You cannot edit another volunteer's profile",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        oldVolunteer.setName(name);
        oldVolunteer.setPhone(phone);
        oldVolunteer.setSkills(skills);
        oldVolunteer.setAvailability(availability);
        oldVolunteer.setLocation(location);

        volunteersRef.child(oldVolunteer.getVolunteerId())
                .setValue(oldVolunteer)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Volunteer profile updated!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Update failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // DELETE: Confirm deletion
    private void confirmDelete(Volunteer volunteer) {

        if (!currentUserId.equals(volunteer.getUserId())) {
            Toast.makeText(this,
                    "You cannot delete another volunteer's profile",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Volunteer Profile")
                .setMessage(
                        "Are you sure you want to delete your volunteer profile?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete",
                        (dialog, which) -> deleteVolunteer(volunteer))
                .show();
    }

    // DELETE: Remove volunteer from Firebase
    private void deleteVolunteer(Volunteer volunteer) {

        volunteersRef.child(volunteer.getVolunteerId())
                .removeValue()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Volunteer profile deleted!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Delete failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (volunteersListener != null && volunteersRef != null) {
            volunteersRef.removeEventListener(volunteersListener);
        }
    }
}
