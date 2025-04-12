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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, goToRegisterButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализиране на Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Инициализиране на Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Проверка дали потребителят е влязъл и има запис в базата
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            databaseReference.child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // Потребителят е влязъл и има запис в базата
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Потребителят е влязъл, но няма запис в базата - изчистваме го
                    auth.signOut();
                    Toast.makeText(LoginActivity.this, "Please register again", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Инициализиране на UI елементите
        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);

        // Логин бутон за email/password
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }

            // Деактивиране на бутона по време на логин
            loginButton.setEnabled(false);

            // Логин с Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        loginButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            // Проверка дали потребителят има запис в базата
                            String userId = auth.getCurrentUser().getUid();
                            databaseReference.child(userId).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful() && task1.getResult().exists()) {
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Няма запис в базата - изчистваме потребителя
                                    auth.signOut();
                                    Toast.makeText(LoginActivity.this, "User data not found, please register again", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "Auth error: " + task.getException().getMessage());
                        }
                    });
        });

        // Пренасочване към регистрация
        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish(); // Затваряме LoginActivity, за да не се връща потребителят тук при натискане на "назад"
        });
    }
}