package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;

public class MultiplayerModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_mode);

        Button createRoomButton = findViewById(R.id.createRoomButton);
        Button joinRoomButton = findViewById(R.id.joinRoomButton);

        createRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(MultiplayerModeActivity.this, CreateRoomActivity.class);
            startActivity(intent);
        });

        joinRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(MultiplayerModeActivity.this, JoinRoomActivity.class);
            startActivity(intent);
        });
    }
}