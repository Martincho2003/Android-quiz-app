package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.viewModel.CreateRoomViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateRoomActivity extends AppCompatActivity {

    private static final String TAG = "CreateRoomActivity";
    private LinearLayout subjectsContainer, difficultiesContainer;
    private Button createRoomButton, startGameButton;
    private TextView titleTextView, waitingTextView, playersTextView;
    private List<CheckBox> subjectCheckBoxes, difficultyCheckBoxes;
    private CreateRoomViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        subjectsContainer = findViewById(R.id.subjectsContainer);
        difficultiesContainer = findViewById(R.id.difficultiesContainer);
        createRoomButton = findViewById(R.id.createRoomButton);
        startGameButton = findViewById(R.id.startGameButton);
        titleTextView = findViewById(R.id.titleTextView);
        waitingTextView = findViewById(R.id.waitingTextView);
        playersTextView = findViewById(R.id.playersTextView);

        subjectCheckBoxes = new ArrayList<>();
        difficultyCheckBoxes = new ArrayList<>();

        for (Subject subject : Subject.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subject.getValue());
            checkBox.setPadding(0, 8, 0, 8);
            subjectsContainer.addView(checkBox);
            subjectCheckBoxes.add(checkBox);
        }

        for (Difficulty difficulty : Difficulty.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(difficulty.getValue());
            checkBox.setPadding(0, 8, 0, 8);
            difficultiesContainer.addView(checkBox);
            difficultyCheckBoxes.add(checkBox);
        }

        viewModel = new ViewModelProvider(this).get(CreateRoomViewModel.class);

        createRoomButton.setOnClickListener(v -> createRoom());
        startGameButton.setOnClickListener(v -> startGame());

        viewModel.getLoadedQuestions().observe(this, questions -> {
            if (questions != null) {
                Log.d(TAG, "Loaded questions successfully, switching to waiting screen");
                switchToWaitingScreen();
            } else {
                Log.e(TAG, "Loaded questions is null");
            }
        });

        viewModel.getRoomCreationFailed().observe(this, failed -> {
            if (failed != null && failed) {
                Log.e(TAG, "Room creation failed");
                Toast.makeText(this, "Failed to create room. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCreatedRoom().observe(this, room -> {
            if (room != null) {
                Log.d(TAG, "Room updated: " + room.getCreatorNickname() + ", players: " + room.getUsers().size());
                updateWaitingScreen(room);
            } else {
                Log.e(TAG, "Room is null in getCreatedRoom observer");
            }
        });
    }

    private void createRoom() {
        List<Subject> selectedSubjects = new ArrayList<>();
        List<Difficulty> selectedDifficulties = new ArrayList<>();

        for (int i = 0; i < subjectCheckBoxes.size(); i++) {
            if (subjectCheckBoxes.get(i).isChecked()) {
                selectedSubjects.add(Subject.values()[i]);
            }
        }

        if (selectedSubjects.isEmpty()) {
            selectedSubjects.addAll(Arrays.asList(Subject.values()));
        }

        for (int i = 0; i < difficultyCheckBoxes.size(); i++) {
            if (difficultyCheckBoxes.get(i).isChecked()) {
                selectedDifficulties.add(Difficulty.values()[i]);
            }
        }

        if (selectedDifficulties.isEmpty()) {
            selectedDifficulties.addAll(Arrays.asList(Difficulty.values()));
        }

        viewModel.createRoom(selectedSubjects, selectedDifficulties);
    }

    private void switchToWaitingScreen() {
        titleTextView.setText("Room: " + viewModel.getCurrentUser().getValue().getUsername());
        subjectsContainer.setVisibility(View.GONE);
        difficultiesContainer.setVisibility(View.GONE);
        createRoomButton.setVisibility(View.GONE);
        waitingTextView.setVisibility(View.VISIBLE);
        playersTextView.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE);
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Players: " + playerCount + "/4");
        startGameButton.setEnabled(playerCount >= 2 && !room.isGameStarted());
        Log.d(TAG, "Updated waiting screen, players: " + playerCount + ", isGameStarted: " + room.isGameStarted());

        if (room.isGameStarted()) {
            Log.d(TAG, "Game started, launching MultiplayerGameActivity");
            Intent intent = new Intent(CreateRoomActivity.this, MultiplayerGameActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
            finish();
        }
    }

    private void startGame() {
        viewModel.startGame();
    }
}