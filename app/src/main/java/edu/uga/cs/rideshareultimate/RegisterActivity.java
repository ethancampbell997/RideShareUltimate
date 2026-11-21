package edu.uga.cs.rideshareultimate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Handles new account registration and initializes 50 starting ride-points in Realtime Database.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final int INITIAL_RIDE_POINTS = 50;

    private EditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnCreateAccount.setOnClickListener(v -> registerUser());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            User newUser = new User(uid, email, INITIAL_RIDE_POINTS);
                            usersRef.child(uid).setValue(newUser)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Account created! You received " + INITIAL_RIDE_POINTS + " ride-points.",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Database error: " + dbTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed.";
                        Toast.makeText(RegisterActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}