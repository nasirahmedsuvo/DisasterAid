
package com.example.disasteraid.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.example.disasteraid.adapters.RequestAdapter;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.example.disasteraid.models.AidRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestListActivity extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private TextView tvEmpty;
    private FloatingActionButton fabAddRequest;

    private List<AidRequest> requestList;
    private RequestAdapter adapter;

    private DatabaseReference requestsRef;
    private ValueEventListener requestsListener;

    private String currentUserId;

    private final String[] categories = {
            "Food", "Water", "Medicine",
            "Shelter", "Clothing", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        currentUserId = FirebaseHelper.getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerRequests = findViewById(R.id.recyclerRequests);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddRequest = findViewById(R.id.fabAddRequest);

        requestsRef = FirebaseHelper.getDatabase()
                .child("aidRequests");

        requestList = new ArrayList<>();

        adapter = new RequestAdapter(
                requestList,
                currentUserId,
                new RequestAdapter.RequestActionListener() {

                    @Override
                    public void onEdit(AidRequest request) {
                        showRequestDialog(request);
                    }

                    @Override
                    public void onDelete(AidRequest request) {
                        confirmDelete(request);
                    }
                }
        );

        recyclerRequests.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerRequests.setAdapter(adapter);

        fabAddRequest.setOnClickListener(v ->
                showRequestDialog(null));

        loadRequests();
    }

    // Load requests from Firebase
    private void loadRequests() {

        requestsListener = new ValueEventListener() {

            @Override
            public void onDataChange(
                    @NonNull DataSnapshot snapshot) {

                requestList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    AidRequest request =
                            data.getValue(AidRequest.class);

                    if (request != null) {
                        request.setRequestId(data.getKey());
                        requestList.add(request);
                    }
                }

                adapter.notifyDataSetChanged();

                if (requestList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerRequests.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerRequests.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(
                    @NonNull DatabaseError error) {

                Toast.makeText(
                        RequestListActivity.this,
                        "Failed to load requests: "
                                + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        requestsRef.addValueEventListener(requestsListener);
    }

    // Create or edit request dialog
    private void showRequestDialog(AidRequest existingRequest) {

        boolean isEditing = existingRequest != null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Request Title");
        layout.addView(etTitle);

        Spinner spCategory = new Spinner(this);

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spCategory.setAdapter(categoryAdapter);
        layout.addView(spCategory);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Describe your emergency");
        etDescription.setMinLines(2);
        etDescription.setGravity(Gravity.TOP);
        layout.addView(etDescription);

        EditText etLocation = new EditText(this);
        etLocation.setHint("Location");
        layout.addView(etLocation);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Quantity");
        etQuantity.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(etQuantity);

        // Fill existing data when editing
        if (isEditing) {

            etTitle.setText(existingRequest.getTitle());
            etDescription.setText(existingRequest.getDescription());
            etLocation.setText(existingRequest.getLocation());
            etQuantity.setText(existingRequest.getQuantity());

            int position = 0;

            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(
                        existingRequest.getCategory())) {
                    position = i;
                    break;
                }
            }

            spCategory.setSelection(position);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing ? "Edit Request" : "Create Request")
                .setView(layout)
                .setPositiveButton(isEditing ? "Update" : "Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String title = etTitle.getText()
                                .toString().trim();

                        String category = spCategory
                                .getSelectedItem().toString();

                        String description = etDescription.getText()
                                .toString().trim();

                        String location = etLocation.getText()
                                .toString().trim();

                        String quantity = etQuantity.getText()
                                .toString().trim();

                        if (title.isEmpty()
                                || description.isEmpty()
                                || location.isEmpty()
                                || quantity.isEmpty()) {

                            Toast.makeText(
                                    this,
                                    "Please fill in all fields",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (isEditing) {
                            updateRequest(
                                    existingRequest,
                                    title,
                                    category,
                                    description,
                                    location,
                                    quantity
                            );
                        } else {
                            createRequest(
                                    title,
                                    category,
                                    description,
                                    location,
                                    quantity
                            );
                        }

                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    // Create a new request
    private void createRequest(
            String title,
            String category,
            String description,
            String location,
            String quantity) {

        String requestId = requestsRef.push().getKey();

        if (requestId == null) {
            Toast.makeText(this, "Unable to create request",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        long currentTime = System.currentTimeMillis();

        AidRequest request = new AidRequest(
                requestId,
                currentUserId,
                title,
                category,
                description,
                location,
                quantity,
                "Pending",
                currentTime,
                currentTime
        );

        requestsRef.child(requestId)
                .setValue(request)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Request created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";

                        Toast.makeText(
                                this,
                                "Failed to create request: " + error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // Update an existing request
    private void updateRequest(
            AidRequest oldRequest,
            String title,
            String category,
            String description,
            String location,
            String quantity) {

        if (!currentUserId.equals(oldRequest.getUserId())) {
            Toast.makeText(this, "You cannot edit this request",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        oldRequest.setTitle(title);
        oldRequest.setCategory(category);
        oldRequest.setDescription(description);
        oldRequest.setLocation(location);
        oldRequest.setQuantity(quantity);
        oldRequest.setUpdatedAt(System.currentTimeMillis());

        requestsRef.child(oldRequest.getRequestId())
                .setValue(oldRequest)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Request updated successfully!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Failed to update request",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // Confirm before deleting
    private void confirmDelete(AidRequest request) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage(
                        "Are you sure you want to delete this request?"
                )
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteRequest(request))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Delete request from Firebase
    private void deleteRequest(AidRequest request) {

        if (!currentUserId.equals(request.getUserId())) {
            Toast.makeText(this, "You cannot delete this request",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        requestsRef.child(request.getRequestId())
                .removeValue()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Request deleted successfully!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Failed to delete request",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestsRef != null && requestsListener != null) {
            requestsRef.removeEventListener(requestsListener);
        }
    }
}
