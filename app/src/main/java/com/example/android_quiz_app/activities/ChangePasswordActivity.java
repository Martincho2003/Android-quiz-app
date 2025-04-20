package com.example.android_quiz_app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_quiz_app.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button changePasswordButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Трябва да сте в приложението, за да промените паролата си.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        changePasswordButton.setOnClickListener(v -> {
            changePassword();
        });
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordEditText.setError("Въведете старата си парола");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("Въведете новата си парола");
            return;
        }

        if (newPassword.length() < 8) {
            newPasswordEditText.setError("Паролата трябва да е поне 8 символа");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordEditText.setError("Паролите не съвпадат");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ChangePasswordActivity.this, "Паролата е променана успешно", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChangePasswordActivity.this, "Неуспешна промяна на паролата: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChangePasswordActivity.this, "Грешна стара парола", Toast.LENGTH_LONG).show();
                });
    }
}