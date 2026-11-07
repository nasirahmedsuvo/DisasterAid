
package com.example.disasteraid.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disasteraid.R;

public class ImagePickerActivity extends AppCompatActivity {

    private ImageView ivSelectedPhoto;
    private Button btnOpenCamera;
    private Button btnOpenGallery;

    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        ivSelectedPhoto.setImageBitmap(bitmap);
                        Toast.makeText(this,
                                "Photo captured successfully!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Camera cancelled or no photo returned.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivSelectedPhoto.setImageURI(uri);
                        Toast.makeText(this,
                                "Gallery photo selected!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "No gallery photo selected.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnOpenCamera.setOnClickListener(v ->
                cameraLauncher.launch(null));

        btnOpenGallery.setOnClickListener(v ->
                galleryLauncher.launch("image/*"));
    }
}
