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
    private Button logoutButton, changePasswordButton;
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
        changePasswordButton = findViewById(R.id.changePasswordButton);

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

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

    }

    private void updateProfileUI(User user) {
        if (user != null) {
            usernameTextView.setText("Потребителско име: " + user.getUsername());
            pointsTextView.setText("Точки: " + user.getPoints());
            lastDayPlayedTextView.setText("Последна игра: " + (user.getLastDayPlayed() != null &&
                    !user.getLastDayPlayed().equals("1.1.1970") ? user.getLastDayPlayed() : "Не е изиграна игра"));
            playedGamesTodayTextView.setText("Игри днес: " + user.getPlayedGamesToday());
        }
    }
}