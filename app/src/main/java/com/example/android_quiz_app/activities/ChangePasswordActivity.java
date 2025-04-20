package com.example.android_quiz_app.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_quiz_app.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.function.BooleanSupplier;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button changePasswordButton;
    private FirebaseAuth auth;
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;

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

        setupPasswordVisibilityToggle(oldPasswordEditText);
        setupPasswordVisibilityToggle(newPasswordEditText);
        setupPasswordVisibilityToggle(confirmNewPasswordEditText);

        changePasswordButton.setOnClickListener(v -> {
            changePassword();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordVisibilityToggle(EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (editText.getCompoundDrawables()[2] != null &&
                            event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width() - editText.getPaddingRight())) {
                        boolean isOldPasswordField = editText.getId() == R.id.oldPasswordEditText;
                        boolean isNewPasswordField = editText.getId() == R.id.newPasswordEditText;
                        boolean isConfirmPasswordField = editText.getId() == R.id.confirmNewPasswordEditText;

                        boolean isVisible;
                        if (isOldPasswordField) {
                            isVisible = isOldPasswordVisible;
                        } else if (isNewPasswordField) {
                            isVisible = isNewPasswordVisible;
                        } else {
                            isVisible = isConfirmNewPasswordVisible;
                        }

                        if (isVisible) {
                            // Hide password
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                        } else {
                            // Show password
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.no_eye, 0);
                        }

                        if (isOldPasswordField) {
                            isOldPasswordVisible = !isOldPasswordVisible;
                        } else if (isNewPasswordField) {
                            isNewPasswordVisible = !isNewPasswordVisible;
                        } else if (isConfirmPasswordField) {
                            isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible;
                        }

                        // Move cursor to the end of the text
                        editText.setSelection(editText.getText().length());
                        return true;
                    }
                }
                return false;
            }
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