package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button registerButton, goToLoginButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализиране на Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Инициализиране на Firebase Database с пълен път
        databaseReference = FirebaseDatabase.getInstance("https://android-quiz-app-8e645-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
        Log.d(TAG, "DatabaseReference initialized: " + databaseReference.toString());

        // Инициализиране на UI елементите
        usernameEditText = findViewById(R.id.registerUsernameEditText);
        emailEditText = findViewById(R.id.registerEmailEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        goToLoginButton = findViewById(R.id.goToLoginButton);

        // Регистрация бутон
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                usernameEditText.setError("Username is required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            // Деактивиране на бутона по време на регистрация
            registerButton.setEnabled(false);
            Log.d(TAG, "Starting registration process for email: " + email);

            // Регистрация с Firebase
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase Authentication successful, user ID: " + auth.getCurrentUser().getUid());
                            // Успешна регистрация в Firebase Authentication
                            String userId = auth.getCurrentUser().getUid();
                            User user = new User(username, 0, null, 0);

                            // Лог преди запис
                            Log.d(TAG, "Attempting to save user data to Realtime Database for user ID: " + userId);

                            // Запазване на потребителските данни в Realtime Database
                            databaseReference.child(userId).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        registerButton.setEnabled(true);
                                        Log.d(TAG, "User data saved successfully in Realtime Database");
                                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                        // Директно прехвърляне към MainActivity
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        registerButton.setEnabled(true);
                                        Log.e(TAG, "Failed to save user data: " + e.getMessage(), e);
                                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                        // Изтриване на потребителя от Firebase Authentication
                                        auth.getCurrentUser().delete()
                                                .addOnCompleteListener(deleteTask -> {
                                                    if (deleteTask.isSuccessful()) {
                                                        Log.d(TAG, "User deleted from Firebase Authentication due to database error");
                                                        Toast.makeText(RegisterActivity.this, "Registration failed, please try again", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Log.e(TAG, "Failed to delete user: " + deleteTask.getException().getMessage());
                                                        Toast.makeText(RegisterActivity.this, "Registration failed, please log out and try again", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    })
                                    .addOnCompleteListener(task1 -> {
                                        Log.d(TAG, "setValue operation completed, success: " + task1.isSuccessful());
                                        if (!task1.isSuccessful()) {
                                            Log.e(TAG, "setValue failed: " + task1.getException().getMessage(), task1.getException());
                                        }
                                    });
                        } else {
                            registerButton.setEnabled(true);
                            Log.e(TAG, "Firebase Authentication failed: " + task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        registerButton.setEnabled(true);
                        Log.e(TAG, "Firebase Authentication failed (onFailure): " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Пренасочване към логин
        goToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}