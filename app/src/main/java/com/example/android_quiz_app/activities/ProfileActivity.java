package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.viewModel.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, pointsTextView, lastDayPlayedTextView, playedGamesTodayTextView;
    private Button logoutButton;
    private ProfileViewModel viewModel;

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
            Toast.makeText(ProfileActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
        });

        viewModel.getCurrentUserInfo();

        logoutButton.setOnClickListener(v -> {
            viewModel.logout();
        });
    }

    private void updateProfileUI(User user) {
        if (user != null) {
            usernameTextView.setText("Username: " + user.getUsername());
            pointsTextView.setText("Points: " + user.getPoints());
            lastDayPlayedTextView.setText("Last Day Played: " + (user.getLastDayPlayed() != null ? user.getLastDayPlayed() : "Never"));
            playedGamesTodayTextView.setText("Games Played Today: " + user.getPlayedGamesToday());
        }
    }
}