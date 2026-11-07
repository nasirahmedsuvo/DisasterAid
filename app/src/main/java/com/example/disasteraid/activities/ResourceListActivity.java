
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
import com.example.disasteraid.adapters.ResourceAdapter;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.example.disasteraid.models.Resource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResourceListActivity extends AppCompatActivity {

    private RecyclerView recyclerResources;
    private TextView tvResourceEmpty;
    private FloatingActionButton fabAddResource;

    private List<Resource> resourceList;
    private ResourceAdapter adapter;

    private DatabaseReference resourcesRef;
    private ValueEventListener resourcesListener;

    private String currentUserId;

    private static final int WHITE = Color.WHITE;
    private static final int PRIMARY = Color.rgb(23, 59, 100);
    private static final int SECONDARY = Color.rgb(22, 160, 133);
    private static final int TEXT_DARK = Color.rgb(36, 50, 71);
    private static final int TEXT_HINT = Color.rgb(123, 135, 152);

    private final String[] categories = {
            "Food",
            "Water",
            "Medicine",
            "Clothing",
            "Shelter Supplies",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_list);

        currentUserId = FirebaseHelper.getCurrentUserId();

        if (currentUserId == null) {
            Toast.makeText(this, "Please login first",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerResources = findViewById(R.id.recyclerResources);
        tvResourceEmpty = findViewById(R.id.tvResourceEmpty);
        fabAddResource = findViewById(R.id.fabAddResource);

        resourcesRef = FirebaseHelper.getDatabase()
                .child("resources");

        resourceList = new ArrayList<>();

        adapter = new ResourceAdapter(
                resourceList,
                currentUserId,
                new ResourceAdapter.ResourceActionListener() {

                    @Override
                    public void onEdit(Resource resource) {
                        showResourceDialog(resource);
                    }

                    @Override
                    public void onDelete(Resource resource) {
                        confirmDelete(resource);
                    }
                }
        );

        recyclerResources.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerResources.setAdapter(adapter);

        fabAddResource.setOnClickListener(v ->
                showResourceDialog(null));

        loadResources();
    }

    // READ: Load resources from Firebase
    private void loadResources() {

        resourcesListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                resourceList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Resource resource =
                            data.getValue(Resource.class);

                    if (resource != null) {
                        resource.setResourceId(data.getKey());
                        resourceList.add(resource);
                    }
                }

                adapter.notifyDataSetChanged();

                tvResourceEmpty.setVisibility(
                        resourceList.isEmpty()
                                ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(
                        ResourceListActivity.this,
                        "Failed to load resources: "
                                + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        resourcesRef.addValueEventListener(resourcesListener);
    }

    // CREATE / UPDATE: Resource form
    private void showResourceDialog(Resource existingResource) {

        boolean isEditing = existingResource != null;

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(40, 20, 40, 10);
        form.setBackgroundColor(WHITE);

        EditText etName = createInput(
                "Resource Name",
                InputType.TYPE_CLASS_TEXT);

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

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spCategory.setAdapter(categoryAdapter);
        spCategory.setBackgroundTintList(
                ColorStateList.valueOf(SECONDARY));

        EditText etQuantity = createInput(
                "Quantity (e.g. 20 food packages)",
                InputType.TYPE_CLASS_TEXT);

        EditText etLocation = createInput(
                "Resource Location",
                InputType.TYPE_CLASS_TEXT);

        form.addView(etName);
        form.addView(spCategory);
        form.addView(etQuantity);
        form.addView(etLocation);

        if (isEditing) {

            etName.setText(existingResource.getResourceName());
            etQuantity.setText(existingResource.getQuantity());
            etLocation.setText(existingResource.getLocation());

            int selected = categoryAdapter.getPosition(
                    existingResource.getCategory());

            if (selected >= 0) {
                spCategory.setSelection(selected);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEditing
                        ? "Edit Resource"
                        : "Add Emergency Resource")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        isEditing ? "Update" : "Add", null)
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

                    String quantity = etQuantity.getText()
                            .toString().trim();

                    String location = etLocation.getText()
                            .toString().trim();

                    String category =
                            spCategory.getSelectedItem().toString();

                    if (name.isEmpty()) {
                        etName.setError("Enter resource name");
                        return;
                    }

                    if (quantity.isEmpty()) {
                        etQuantity.setError("Enter quantity");
                        return;
                    }

                    if (location.isEmpty()) {
                        etLocation.setError("Enter location");
                        return;
                    }

                    if (isEditing) {
                        updateResource(
                                existingResource,
                                name,
                                category,
                                quantity,
                                location);
                    } else {
                        createResource(
                                name,
                                category,
                                quantity,
                                location);
                    }

                    dialog.dismiss();
                });
            }
        });

        dialog.show();
    }

    // Reusable input with readable colors
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

    // CREATE: Save resource
    private void createResource(
            String name,
            String category,
            String quantity,
            String location) {

        String resourceId = resourcesRef.push().getKey();

        if (resourceId == null) {
            Toast.makeText(this,
                    "Could not create resource ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Resource resource = new Resource(
                resourceId,
                currentUserId,
                name,
                category,
                quantity,
                location,
                System.currentTimeMillis()
        );

        resourcesRef.child(resourceId)
                .setValue(resource)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Resource added successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to add resource",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // UPDATE: Update own resource
    private void updateResource(
            Resource oldResource,
            String name,
            String category,
            String quantity,
            String location) {

        if (!currentUserId.equals(oldResource.getUserId())) {
            Toast.makeText(this,
                    "You cannot edit another user's resource",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        oldResource.setResourceName(name);
        oldResource.setCategory(category);
        oldResource.setQuantity(quantity);
        oldResource.setLocation(location);

        resourcesRef.child(oldResource.getResourceId())
                .setValue(oldResource)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Resource updated successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to update resource",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // DELETE: Confirm deletion
    private void confirmDelete(Resource resource) {

        if (!currentUserId.equals(resource.getUserId())) {
            Toast.makeText(this,
                    "You cannot delete another user's resource",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Resource")
                .setMessage(
                        "Are you sure you want to delete this resource?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete",
                        (dialog, which) -> deleteResource(resource))
                .show();
    }

    // DELETE: Remove resource from Firebase
    private void deleteResource(Resource resource) {

        resourcesRef.child(resource.getResourceId())
                .removeValue()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Resource deleted successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Failed to delete resource",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (resourcesListener != null && resourcesRef != null) {
            resourcesRef.removeEventListener(resourcesListener);
        }
    }
}
