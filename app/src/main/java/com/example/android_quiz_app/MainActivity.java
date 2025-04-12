package com.example.android_quiz_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.activities.GameActivity;
import com.example.android_quiz_app.activities.LeaderboardActivity;
import com.example.android_quiz_app.activities.LoginActivity;
import com.example.android_quiz_app.activities.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button profileButton, startGameButton, leaderboardButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализиране на Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Проверка дали потребителят е влязъл
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Инициализиране на бутоните
        profileButton = findViewById(R.id.profileButton);
        startGameButton = findViewById(R.id.startGameButton);
        leaderboardButton = findViewById(R.id.leaderboardButton);

        // Обработка на бутоните
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        startGameButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GameActivity.class));
        });

        leaderboardButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
        });

        // Обработка на бутона "назад" с OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}