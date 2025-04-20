package com.example.android_quiz_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.MotionEvent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.viewModel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, goToRegisterButton, googleLoginButton;
    private TextView forgotPasswordTextView;
    private LoginViewModel viewModel;
    private boolean isPasswordVisible = false;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                try {
                    com.google.android.gms.auth.api.signin.GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                    if (account != null) {
                        String idToken = account.getIdToken();
                        viewModel.loginWithGoogle(idToken);
                    } else {
                        Toast.makeText(this, "Неуспешен вход с Google: Не е избран акаунт", Toast.LENGTH_LONG).show();
                    }
                } catch (ApiException e) {
                    Toast.makeText(this, "Неуспешен вход с Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Входът с Google е прекъснат", Toast.LENGTH_LONG).show();
            }
        });


        viewModel.getLoginState().observe(this, state -> {
            loginButton.setEnabled(true);
            googleLoginButton.setEnabled(true);
            Toast.makeText(LoginActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
            if (state.isSuccess()) {
                if (state.getMessage().equals("Успешен вход с Google")) {
                    Intent intent = new Intent(LoginActivity.this, UsernameSelectActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            loginButton.setEnabled(false);
            viewModel.login(email, password);
        });

        googleLoginButton.setOnClickListener(v -> {
            googleLoginButton.setEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        forgotPasswordTextView.setOnClickListener(v -> { showResetPasswordDialog();});
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Нулиране на парола");

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Въведете имейл");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(40, 40, 40, 40);
        builder.setView(emailInput);

        builder.setPositiveButton("Изпрати", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            viewModel.resetPassword(email);
        });
        builder.setNegativeButton("Отмени", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.password_lock, 0, R.drawable.eye, 0);
            isPasswordVisible = false;
        } else {
            passwordEditText.setTransformationMethod(SingleLineTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.password_lock, 0, R.drawable.no_eye, 0);
            isPasswordVisible = true;
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }
}