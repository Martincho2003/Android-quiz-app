package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.viewModel.ProfileViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, pointsTextView, lastDayPlayedTextView, playedGamesTodayTextView;
    private Button logoutButton, changePasswordButton;
    private ProfileViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private ImageView addProfileImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        usernameTextView = findViewById(R.id.usernameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        lastDayPlayedTextView = findViewById(R.id.lastDayPlayedTextView);
        playedGamesTodayTextView = findViewById(R.id.playedGamesTodayTextView);
        logoutButton = findViewById(R.id.logoutButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        addProfileImageButton = findViewById(R.id.addProfileImageButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(String.valueOf(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        viewModel.getProfileState().observe(this, state -> {
            if (state.isSuccess()) {
                updateProfileUI(state.getUser());
                if(state.getMessage().equals("Logged out successfully")) {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
            if(!state.isGoogleUser()) {
                changePasswordButton.setVisibility(View.VISIBLE);
            }
            Toast.makeText(ProfileActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
        });

        viewModel.getCurrentUserInfo();

        logoutButton.setOnClickListener(v -> {

            viewModel.logout(googleSignInClient);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        addProfileImageButton.setOnClickListener(v -> {
            Toast.makeText(this, "Добави профилна снимка", Toast.LENGTH_SHORT).show();
            // TODO: Логика за избор на профилна снимка
        });

    }

    private void updateProfileUI(User user) {
        if (user != null) {
            usernameTextView.setText("Потребителско име: " + user.getUsername());
            pointsTextView.setText("Точки: " + user.getPoints());
            lastDayPlayedTextView.setText("Последна игра: " + (user.getLastDayPlayed() != null &&
                    !user.getLastDayPlayed().equals("1.1.1970") ? user.getLastDayPlayed() : "\nНе е играл/а никога"));
            playedGamesTodayTextView.setText("Брой игри за дадения ден: " + user.getPlayedGamesToday());
        }
    }
}