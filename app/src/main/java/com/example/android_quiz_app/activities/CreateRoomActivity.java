package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
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
    private LinearLayout subjectsContainer, difficultiesContainer, waitingInfoContainer;
    private Button createRoomButton, startGameButton, deleteRoomButton;

    private LottieAnimationView waitingAnimationView;
    private TextView titleTextView, waitingTextView, playersTextView, subjectsLabel, difficultiesLabel;
    private TextView selectedSubjectsTextView, selectedDifficultiesTextView, playersListTextView;
    private TextView warningText;
    private List<CheckBox> subjectCheckBoxes, difficultyCheckBoxes;
    private CreateRoomViewModel viewModel;
    private boolean isWaitingScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        subjectsContainer = findViewById(R.id.subjectsContainer);
        difficultiesContainer = findViewById(R.id.difficultiesContainer);
        waitingInfoContainer = findViewById(R.id.waitingInfoContainer);
        createRoomButton = findViewById(R.id.createRoomButton);
        startGameButton = findViewById(R.id.startGameButton);
        titleTextView = findViewById(R.id.titleTextView);
        waitingTextView = findViewById(R.id.waitingTextView);
        playersTextView = findViewById(R.id.playersTextView);
        subjectsLabel = findViewById(R.id.subjectsLabel);
        difficultiesLabel = findViewById(R.id.difficultiesLabel);
        selectedSubjectsTextView = findViewById(R.id.selectedSubjectsTextView);
        selectedDifficultiesTextView = findViewById(R.id.selectedDifficultiesTextView);
        playersListTextView = findViewById(R.id.playersListTextView);
        deleteRoomButton = findViewById(R.id.deleteRoomButton);
        warningText = findViewById(R.id.warningText);
        waitingAnimationView = findViewById(R.id.waitingAnimationView);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isWaitingScreen) {
                    deleteRoom();
                } else {
                    finish();
                }
            }
        });

        subjectCheckBoxes = new ArrayList<>();
        difficultyCheckBoxes = new ArrayList<>();

        for (Subject subject : Subject.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subject.getValue());
            checkBox.setPadding(8, 8, 8, 8);
            checkBox.setTextSize(20);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setButtonDrawable(R.drawable.custom_checkbox);
            checkBox.setCompoundDrawablePadding(24);

            switch (subject) {
                case BIOLOGY:
                    checkBox.setText(R.string.subject_biology);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.biology, 0, 0, 0);
                    break;
                case HISTORY:
                    checkBox.setText(R.string.subject_history);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.history, 0, 0, 0);
                    break;
                case GEOGRAPHY:
                    checkBox.setText(R.string.subject_geography);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.geography, 0, 0, 0);
                    break;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            checkBox.setLayoutParams(params);

            subjectsContainer.addView(checkBox);
            subjectCheckBoxes.add(checkBox);
        }

        for (Difficulty difficulty : Difficulty.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(difficulty.getValue());
            checkBox.setPadding(8, 8, 8, 8);
            checkBox.setTextSize(20);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setButtonDrawable(R.drawable.custom_checkbox);
            checkBox.setCompoundDrawablePadding(24);

            switch (difficulty) {
                case EASY:
                    checkBox.setText(R.string.difficulty_easy);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.angel, 0, 0, 0);
                    break;
                case HARD:
                    checkBox.setText(R.string.difficulty_hard);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.devil, 0, 0, 0);
                    break;
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            checkBox.setLayoutParams(params);

            difficultiesContainer.addView(checkBox);
            difficultyCheckBoxes.add(checkBox);
        }

        viewModel = new ViewModelProvider(this).get(CreateRoomViewModel.class);

        createRoomButton.setOnClickListener(v -> createRoom());
        startGameButton.setOnClickListener(v -> startGame());
        deleteRoomButton.setOnClickListener(v -> deleteRoom());

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
                Toast.makeText(this, "Неуспешно създаване на стая. Моля опитайте отново.", Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCreatedRoom().observe(this, room -> {
            if (room != null) {
                Log.d(TAG, "Room updated: " + room.getCreatorNickname() + ", players: " + room.getUsers().size());
                updateWaitingScreen(room);
                isWaitingScreen = true;
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

        viewModel.createRoom(selectedSubjects, selectedDifficulties, this);
    }

    private void switchToWaitingScreen() {
        titleTextView.setText("Стая: " + viewModel.getCurrentUser().getValue().getUsername());
        subjectsLabel.setVisibility(View.GONE);
        difficultiesLabel.setVisibility(View.GONE);
        subjectsContainer.setVisibility(View.GONE);
        difficultiesContainer.setVisibility(View.GONE);
        createRoomButton.setVisibility(View.GONE);
        warningText.setVisibility(View.GONE);
        waitingInfoContainer.setVisibility(View.VISIBLE);
        startGameButton.setVisibility(View.VISIBLE);
        deleteRoomButton.setVisibility(View.VISIBLE);
        waitingAnimationView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Switched to waiting screen");
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Играчи: " + playerCount + "/4");
        waitingTextView.setText(playerCount < 4 ? "Очакване на играчи..." : "Готови за игра!");
        startGameButton.setEnabled(playerCount >= 2 && !room.isGameStarted());
        StringBuilder subjectsText = new StringBuilder("Предмети: ");
        List<Subject> subjects = room.getSubjects();
        for (int i = 0; i < subjects.size(); i++) {
            Subject subject = subjects.get(i);
            String subjectName;
            switch (subject) {
                case BIOLOGY:
                    subjectName = getString(R.string.subject_biology);
                    break;
                case HISTORY:
                    subjectName = getString(R.string.subject_history);
                    break;
                case GEOGRAPHY:
                    subjectName = getString(R.string.subject_geography);
                    break;
                default:
                    subjectName = subject.getValue();
            }
            subjectsText.append(subjectName);
            if (i < subjects.size() - 1) {
                subjectsText.append(", ");
            }
        }
        selectedSubjectsTextView.setText(subjectsText.toString());
        StringBuilder difficultiesText = new StringBuilder("Трудности: ");
        List<Difficulty> difficulties = room.getDifficulties();
        for (int i = 0; i < difficulties.size(); i++) {
            Difficulty difficulty = difficulties.get(i);
            String difficultyName;
            switch (difficulty) {
                case EASY:
                    difficultyName = getString(R.string.difficulty_easy);
                    break;
                case HARD:
                    difficultyName = getString(R.string.difficulty_hard);
                    break;
                default:
                    difficultyName = difficulty.getValue();
            }
            difficultiesText.append(difficultyName);
            if (i < difficulties.size() - 1) {
                difficultiesText.append(", ");
            }
        }

        selectedDifficultiesTextView.setText(difficultiesText.toString());
        String playersList = "Играчи в стаята:\n";
        for (int i = 0; i < room.getUsers().size(); i++) {
            playersList += room.getUsers().get(i).getUsername();
            if (i < room.getUsers().size() - 1) {
                playersList += "\n";
            }
        }
        playersListTextView.setText(playersList);

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

    private void deleteRoom() {
        Room room = viewModel.getCreatedRoom().getValue();
        if (room != null) {
            viewModel.deleteRoom(room);
            Toast.makeText(this, "Стаята е изтрита", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.e(TAG, "Не може да се изтрие стая: стаята е null");
            Toast.makeText(this, "Неуспешно изтриване на стая", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (waitingAnimationView != null) {
            waitingAnimationView.pauseAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingAnimationView != null && isWaitingScreen) {
            waitingAnimationView.playAnimation();
        }
    }
}