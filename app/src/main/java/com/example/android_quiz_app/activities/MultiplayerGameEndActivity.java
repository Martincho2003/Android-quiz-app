package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.repository.MultiplayerService;
import com.google.firebase.database.core.view.View;

import java.util.List;

public class MultiplayerGameEndActivity extends AppCompatActivity {

    private static final String TAG = "MultiplayerGameEndActivity";
    private TextView leaderboardTextView;
    private Room room;
    private MultiplayerService multiplayerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game_end);

        leaderboardTextView = findViewById(R.id.leaderboardTextView);

        // Вземане на Room от Intent
        room = (Room) getIntent().getSerializableExtra("room");
        if (room == null) {
            Log.e(TAG, "Room is null in MultiplayerGameEndActivity");
            finish();
            return;
        }
        Log.d(TAG, "Room received: " + room.getCreatorNickname());

        multiplayerService = MultiplayerService.getInstance();

        // Наблюдение за промени в стаите
        multiplayerService.getRooms().observe(this, rooms -> {
            for (Room updatedRoom : rooms) {
                if (updatedRoom.getCreatorNickname().equals(room.getCreatorNickname())) {
                    room = updatedRoom; // Актуализиране на локалния Room
                    updateLeaderboard();
                    break;
                }
            }
        });

        updateLeaderboard();
    }

    private void updateLeaderboard() {
        StringBuilder message = new StringBuilder();
        message.append("Your score: ").append(getCurrentUserPoints()).append(" points\n\n");
        message.append("Leaderboard:\n");
        List<MultiplayerUser> users = room.getUsers();
        if (users != null) {
            users.sort((u1, u2) -> Integer.compare(u2.getGamePoints(), u1.getGamePoints()));
            for (int i = 0; i < Math.min(3, users.size()); i++) {
                MultiplayerUser user = users.get(i);
                message.append((i + 1)).append(". ").append(user.getUsername())
                        .append(" - ").append(user.getGamePoints()).append(" points\n");
            }
        }
        leaderboardTextView.setText(message.toString());
    }

    private int getCurrentUserPoints() {
        String currentUserId = multiplayerService.getCurrentUserId();
        if (currentUserId != null) {
            for (MultiplayerUser user : room.getUsers()) {
                if (user.getUsername().equals(currentUserId)) {
                    return user.getGamePoints();
                }
            }
        }
        return 0;
    }
    public void onBackToMainClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}