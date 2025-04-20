package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.viewModel.JoinRoomViewModel;

import java.util.ArrayList;
import java.util.List;

public class JoinRoomActivity extends AppCompatActivity {

    private static final String TAG = "JoinRoomActivity";
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private JoinRoomViewModel viewModel;
    private LinearLayout roomListLayout, waitingLayout;
    private TextView roomTitleTextView, playersTextView;
    private LottieAnimationView joinAnimation, waitingAnimation1, waitingAnimation2, waitingAnimation3, waitingAnimation4;
    private TextView selectedSubjectsTextView, selectedDifficultiesTextView, playersListTextView;
    private Button leaveRoomButton;
    private SearchView roomSearchView;
    private boolean isWaitingScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        roomsRecyclerView = findViewById(R.id.roomsRecyclerView);
        roomListLayout = findViewById(R.id.roomListLayout);
        waitingLayout = findViewById(R.id.waitingLayout);
        roomTitleTextView = findViewById(R.id.roomTitleTextView);
        playersTextView = findViewById(R.id.playersTextView);
        joinAnimation = findViewById(R.id.joinAnimation);
        waitingAnimation1 = findViewById(R.id.waitingAnimation1);
        waitingAnimation2 = findViewById(R.id.waitingAnimation2);
        waitingAnimation3 = findViewById(R.id.waitingAnimation3);
        waitingAnimation4 = findViewById(R.id.waitingAnimation4);
        selectedSubjectsTextView = findViewById(R.id.selectedSubjectsTextView);
        selectedDifficultiesTextView = findViewById(R.id.selectedDifficultiesTextView);
        playersListTextView = findViewById(R.id.playersListTextView);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);
        roomSearchView = findViewById(R.id.roomSearchView);

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isWaitingScreen) {
                    leaveRoom();
                } else {
                    finish();
                }
            }
        });

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(this, this::joinRoom);
        roomsRecyclerView.setAdapter(roomAdapter);

        viewModel = new ViewModelProvider(this).get(JoinRoomViewModel.class);

        viewModel.getRooms().observe(this, rooms -> {
            Log.d(TAG, "Rooms updated: " + (rooms != null ? rooms.size() : 0));
            List<Room> filteredRooms = new ArrayList<>();

            for (Room room : rooms) {
                if (!room.isGameStarted() && room.getUsers().size() < 4) {
                    filteredRooms.add(room);
                }
            }
            roomAdapter.setRooms(filteredRooms);
            roomsRecyclerView.setContentDescription(filteredRooms.isEmpty() ?
                    getString(R.string.empty_rooms_list) :
                    getString(R.string.rooms_list));
        });

        viewModel.getJoinedRoom().observe(this, room -> {
            if (room != null) {
                Log.d(TAG, "Joined room updated: " + room.getCreatorNickname() + ", isGameStarted: " + room.isGameStarted());
                switchToWaitingScreen(room);
                updateWaitingScreen(room);

                if (room.isGameStarted()) {
                    Log.d(TAG, "Game started, launching MultiplayerGameActivity");
                    Intent intent = new Intent(JoinRoomActivity.this, MultiplayerGameActivity.class);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                }
            } else {
                Log.e(TAG, "Joined room is null");
            }
        });

        setupSearchView();

        leaveRoomButton.setOnClickListener(v -> leaveRoom());
    }

    private void setupSearchView() {
        roomSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                roomAdapter.filterRooms(newText);
                return true;
            }
        });
    }

    private void joinRoom(Room room) {
        if (viewModel.getCurrentUser().getValue() == null) {
            Toast.makeText(this, "Потребителят не е зареден. Моля, изчакайте.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room.getUsers().size() >= 4) {
            Toast.makeText(this, "Стаята е пълна", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room.isGameStarted()) {
            Toast.makeText(this, "Играта вече е започнала.", Toast.LENGTH_SHORT).show();
            return;
        }

        int check = viewModel.joinRoom(room);

        if(check == 0){
            Toast.makeText(this, "Присъединихте се към стаята!", Toast.LENGTH_SHORT).show();
        }else if(check == -1){
            Toast.makeText(this, "Не може да се присъедините към своята стая!", Toast.LENGTH_SHORT).show();
        }
    }

    private void leaveRoom() {
        Room room = viewModel.getJoinedRoom().getValue();
        if (room != null) {
            viewModel.leaveRoom(room);
            Toast.makeText(this, "Напуснахте стаята", Toast.LENGTH_SHORT).show();
            switchToRoomList();
        }
    }

    private void switchToWaitingScreen(Room room) {
        roomListLayout.setVisibility(LinearLayout.GONE);
        waitingLayout.setVisibility(LinearLayout.VISIBLE);
        joinAnimation.setVisibility(LinearLayout.GONE);

        waitingAnimation1.setVisibility(LinearLayout.VISIBLE);
        waitingAnimation2.setVisibility(LinearLayout.VISIBLE);
        waitingAnimation3.setVisibility(LinearLayout.VISIBLE);
        waitingAnimation4.setVisibility(LinearLayout.VISIBLE);

        waitingAnimation1.setAnimation(R.raw.waiting);
        waitingAnimation2.setAnimation(R.raw.waiting);
        waitingAnimation3.setAnimation(R.raw.waiting);
        waitingAnimation4.setAnimation(R.raw.waiting);

        waitingAnimation1.playAnimation();
        waitingAnimation2.playAnimation();
        waitingAnimation3.playAnimation();
        waitingAnimation4.playAnimation();

        waitingLayout.setAlpha(0f);
        waitingLayout.animate()
                .alpha(1f)
                .setDuration(1000)
                .start();

        playersTextView.setVisibility(TextView.VISIBLE);
        selectedSubjectsTextView.setVisibility(TextView.VISIBLE);
        selectedDifficultiesTextView.setVisibility(TextView.VISIBLE);
        playersListTextView.setVisibility(TextView.VISIBLE);
        roomTitleTextView.setText("Стая: " + room.getCreatorNickname());
        leaveRoomButton.setVisibility(View.VISIBLE);
        isWaitingScreen = true;
    }

    private void switchToRoomList() {

        waitingAnimation1.cancelAnimation();
        waitingAnimation2.cancelAnimation();
        waitingAnimation3.cancelAnimation();
        waitingAnimation4.cancelAnimation();

        waitingAnimation1.setVisibility(View.GONE);
        waitingAnimation2.setVisibility(View.GONE);
        waitingAnimation3.setVisibility(View.GONE);
        waitingAnimation4.setVisibility(View.GONE);

        roomListLayout.setVisibility(LinearLayout.VISIBLE);
        waitingLayout.setVisibility(LinearLayout.GONE);
        playersTextView.setVisibility(TextView.GONE);
        selectedSubjectsTextView.setVisibility(TextView.GONE);
        selectedDifficultiesTextView.setVisibility(TextView.GONE);
        playersListTextView.setVisibility(TextView.GONE);
        leaveRoomButton.setVisibility(View.GONE);
        isWaitingScreen = false;

        joinAnimation.setVisibility(View.VISIBLE);
        joinAnimation.playAnimation();
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Играчи: " + playerCount + "/4");
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
            Intent intent = new Intent(JoinRoomActivity.this, MultiplayerGameActivity.class);
            intent.putExtra("room", room);
            startActivity(intent);
            finish();
        }
    }
}