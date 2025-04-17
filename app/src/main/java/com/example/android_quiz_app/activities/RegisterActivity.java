package com.example.android_quiz_app.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.viewModel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, goToLoginButton;
    private RegisterViewModel viewModel;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        usernameEditText = findViewById(R.id.registerUsernameEditText);
        emailEditText = findViewById(R.id.registerEmailEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        goToLoginButton = findViewById(R.id.goToLoginButton);


        setupPasswordToggle(passwordEditText);
        setupPasswordToggle(confirmPasswordEditText);


        viewModel.getRegistrationState().observe(this, state -> {
            registerButton.setEnabled(true);
            Toast.makeText(RegisterActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
            if (state.isSuccess()) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();


            if (password.equals(confirmPassword)) {
                registerButton.setEnabled(false);
                viewModel.register(username, email, password,password);
            } else {
                Toast.makeText(RegisterActivity.this, "Паролите не съвпадат", Toast.LENGTH_LONG).show();
            }
        });


        goToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (editText.getCompoundDrawables()[2] != null &&
                            event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        boolean isPasswordField = editText.getId() == R.id.registerPasswordEditText;
                        boolean isVisible = isPasswordField ? isPasswordVisible : isConfirmPasswordVisible;

                        if (isVisible) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.password_lock, 0, R.drawable.eye, 0
                            );
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.password_lock, 0, R.drawable.no_eye, 0
                            );
                        }
                        if (isPasswordField) {
                            isPasswordVisible = !isPasswordVisible;
                        } else {
                            isConfirmPasswordVisible = !isConfirmPasswordVisible;
                        }
                        editText.setSelection(editText.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });
    }
}