package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;

public class GameModeActivity extends AppCompatActivity {

    private Button singlePlayerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);

        singlePlayerButton = findViewById(R.id.singlePlayerButton);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameModeActivity.this, QuestionSelectionActivity.class);
            startActivity(intent);
        });
    }
}