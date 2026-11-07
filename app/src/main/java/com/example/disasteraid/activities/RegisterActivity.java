package com.example.disasteraid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disasteraid.R;
import com.example.disasteraid.models.User;
import com.example.disasteraid.helpers.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone, etPassword;
    Spinner spinnerRole;
    Button btnRegister;
    TextView tvGoLogin;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseHelper.getAuth();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        String[] roles = {
                "Affected Person",
                "Volunteer",
                "Resource Provider"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty()
                || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();

                        User user = new User(
                                uid,
                                name,
                                email,
                                phone,
                                role,
                                System.currentTimeMillis()
                        );

                        FirebaseHelper.getDatabase()
                                .child("users")
                                .child(uid)
                                .setValue(user)
                                .addOnCompleteListener(saveTask -> {

                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(this,
                                                "Registration successful!",
                                                Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(
                                                this, HomeActivity.class));

                                        finish();

                                    } else {
                                        Toast.makeText(this,
                                                "Profile saving failed: "
                                                        + saveTask.getException()
                                                        .getMessage(),
                                                Toast.LENGTH_LONG).show();

                                        // Remove the incomplete account
                                        // so the user can register again.
                                        auth.getCurrentUser().delete();
                                    }
                                });

                    } else {
                        btnRegister.setEnabled(true);

                        Toast.makeText(this,
                                "Registration failed: "
                                        + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}