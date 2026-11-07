
package com.example.disasteraid.activities;

import android.os.Bundle;
import android.text.InputType;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

    // App colors
    private static final int PRIMARY = Color.rgb(23, 59, 100);
    private static final int SECONDARY = Color.rgb(22, 160, 133);
    private static final int TEXT_DARK = Color.rgb(36, 50, 71);
    private static final int TEXT_HINT = Color.rgb(123, 135, 152);
    private static final int WHITE = Color.WHITE;

    private final String[] categories = {
            "Food",
            "Water",
            "Medicine",
            "Shelter",
            "Clothing",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        currentUserId = FirebaseHelper.getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this,
                    "Please login first",
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
                new LinearLayoutManager(this)
        );

        recyclerRequests.setAdapter(adapter);

        fabAddRequest.setOnClickListener(v ->
                showRequestDialog(null));

        loadRequests();
    }

    // READ: Load requests from Firebase
    private void loadRequests() {

        requestsListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        RequestListActivity.this,
                        "Failed to load requests: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        requestsRef.addValueEventListener(requestsListener);
    }

    // CREATE / UPDATE: Show request form
    private void showRequestDialog(AidRequest existingRequest) {

        boolean isEditing = existingRequest != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(40, 20, 40, 10);
        form.setBackgroundColor(WHITE);

        EditText etTitle = createInput(
                "Request Title",
                InputType.TYPE_CLASS_TEXT
        );

        Spinner spCategory = new Spinner(this);

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                ) {

                    @NonNull
                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            @NonNull android.view.ViewGroup parent) {

                        TextView text = (TextView) super.getView(
                                position, convertView, parent
                        );

                        text.setTextColor(TEXT_DARK);
                        text.setTextSize(15);
                        text.setPadding(8, 12, 8, 12);

                        return text;
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            @NonNull android.view.ViewGroup parent) {

                        TextView text = (TextView) super.getDropDownView(
                                position, convertView, parent
                        );

                        text.setTextColor(TEXT_DARK);
                        text.setBackgroundColor(WHITE);
                        text.setTextSize(15);
                        text.setPadding(20, 16, 20, 16);

                        return text;
                    }
                };

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);

        spCategory.setBackgroundTintList(
                ColorStateList.valueOf(SECONDARY)
        );

        EditText etDescription = createInput(
                "Describe your emergency",
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        etDescription.setMinLines(2);

        EditText etLocation = createInput(
                "Location",
                InputType.TYPE_CLASS_TEXT
        );

        EditText etQuantity = createInput(
                "Quantity (example: 10 people or 5 boxes)",
                InputType.TYPE_CLASS_TEXT
        );

        form.addView(etTitle);
        form.addView(spCategory);
        form.addView(etDescription);
        form.addView(etLocation);
        form.addView(etQuantity);

        if (isEditing) {

            etTitle.setText(existingRequest.getTitle());
            etDescription.setText(existingRequest.getDescription());
            etLocation.setText(existingRequest.getLocation());
            etQuantity.setText(existingRequest.getQuantity());

            int selectedCategory = categoryAdapter.getPosition(
                    existingRequest.getCategory()
            );

            if (selectedCategory >= 0) {
                spCategory.setSelection(selectedCategory);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing
                        ? "Edit Emergency Request"
                        : "Create Emergency Request")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        isEditing ? "Update" : "Create",
                        null
                )
                .create();

        dialog.setOnShowListener(d -> {

            // Set dialog background to white
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(WHITE)
                );
            }

            // Set dialog title color
            TextView titleView = dialog.findViewById(
                    androidx.appcompat.R.id.alertTitle
            );

            if (titleView != null) {
                titleView.setTextColor(PRIMARY);
                titleView.setTextSize(20);
            }

            // Style Cancel button
            Button cancelButton = dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE
            );

            if (cancelButton != null) {
                cancelButton.setTextColor(SECONDARY);
            }

            // Style Create / Update button
            Button saveButton = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            if (saveButton != null) {
                saveButton.setTextColor(SECONDARY);

                saveButton.setOnClickListener(v -> {

                    String title = etTitle.getText()
                            .toString().trim();

                    String description = etDescription.getText()
                            .toString().trim();

                    String location = etLocation.getText()
                            .toString().trim();

                    String quantity = etQuantity.getText()
                            .toString().trim();

                    String category =
                            spCategory.getSelectedItem().toString();

                    // Input validation
                    if (title.isEmpty()) {
                        etTitle.setError("Enter a request title");
                        return;
                    }

                    if (description.isEmpty()) {
                        etDescription.setError("Enter a description");
                        return;
                    }

                    if (location.isEmpty()) {
                        etLocation.setError("Enter your location");
                        return;
                    }

                    if (quantity.isEmpty()) {
                        etQuantity.setError(
                                "Enter the required quantity"
                        );
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
            }
        });

        dialog.show();
    }

    // Reusable input field with corrected colors
    private EditText createInput(String hint, int inputType) {

        EditText editText = new EditText(this);

        editText.setHint(hint);
        editText.setInputType(inputType);

        editText.setTextColor(TEXT_DARK);
        editText.setHintTextColor(TEXT_HINT);

        editText.setTextSize(15);

        editText.setBackgroundTintList(
                ColorStateList.valueOf(SECONDARY)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = 12;

        editText.setLayoutParams(params);

        return editText;
    }

    // CREATE: Save a new request
    private void createRequest(
            String title,
            String category,
            String description,
            String location,
            String quantity) {

        String requestId = requestsRef.push().getKey();

        if (requestId == null) {
            Toast.makeText(this,
                    "Could not create request ID",
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
                        Toast.makeText(this,
                                "Request created successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to create request",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // UPDATE: Update an existing request
    private void updateRequest(
            AidRequest oldRequest,
            String title,
            String category,
            String description,
            String location,
            String quantity) {

        if (!currentUserId.equals(oldRequest.getUserId())) {
            Toast.makeText(this,
                    "You cannot edit another user's request",
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
                        Toast.makeText(this,
                                "Request updated successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to update request",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // DELETE: Ask for confirmation
    private void confirmDelete(AidRequest request) {

        if (!currentUserId.equals(request.getUserId())) {
            Toast.makeText(this,
                    "You cannot delete another user's request",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage(
                        "Are you sure you want to delete this request?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteRequest(request))
                .show();
    }

    // DELETE: Remove request from Firebase
    private void deleteRequest(AidRequest request) {

        requestsRef.child(request.getRequestId())
                .removeValue()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Request deleted successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to delete request",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (requestsListener != null && requestsRef != null) {
            requestsRef.removeEventListener(requestsListener);
        }
    }
}
