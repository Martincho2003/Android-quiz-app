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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Room;
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
    private TextView selectedSubjectsTextView, selectedDifficultiesTextView, playersListTextView;
    private Button leaveRoomButton;
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
        selectedSubjectsTextView = findViewById(R.id.selectedSubjectsTextView);
        selectedDifficultiesTextView = findViewById(R.id.selectedDifficultiesTextView);
        playersListTextView = findViewById(R.id.playersListTextView);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);

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
        roomAdapter = new RoomAdapter(this::joinRoom);
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

        leaveRoomButton.setOnClickListener(v -> leaveRoom());
    }

    private void joinRoom(Room room) {
        if (viewModel.getCurrentUser().getValue() == null) {
            Toast.makeText(this, "User not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room.getUsers().size() >= 4) {
            Toast.makeText(this, "Room is full.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room.isGameStarted()) {
            Toast.makeText(this, "Game has already started.", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.joinRoom(room);
        Toast.makeText(this, "Joined room: " + room.getCreatorNickname(), Toast.LENGTH_SHORT).show();
    }

    private void leaveRoom() {
        Room room = viewModel.getJoinedRoom().getValue();
        if (room != null) {
            viewModel.leaveRoom(room);
            Toast.makeText(this, "Left the room", Toast.LENGTH_SHORT).show();
            switchToRoomList();
        }
    }

    private void switchToWaitingScreen(Room room) {
        roomListLayout.setVisibility(LinearLayout.GONE);
        waitingLayout.setVisibility(LinearLayout.VISIBLE);
        playersTextView.setVisibility(TextView.VISIBLE);
        selectedSubjectsTextView.setVisibility(TextView.VISIBLE);
        selectedDifficultiesTextView.setVisibility(TextView.VISIBLE);
        playersListTextView.setVisibility(TextView.VISIBLE);
        roomTitleTextView.setText("Room: " + room.getCreatorNickname());
        leaveRoomButton.setVisibility(View.VISIBLE);
        isWaitingScreen = true;
    }

    private void switchToRoomList() {
        roomListLayout.setVisibility(LinearLayout.VISIBLE);
        waitingLayout.setVisibility(LinearLayout.GONE);
        playersTextView.setVisibility(TextView.GONE);
        selectedSubjectsTextView.setVisibility(TextView.GONE);
        selectedDifficultiesTextView.setVisibility(TextView.GONE);
        playersListTextView.setVisibility(TextView.GONE);
        leaveRoomButton.setVisibility(View.GONE);
        isWaitingScreen = false;
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Players: " + playerCount + "/4");
        selectedSubjectsTextView.setText("Subjects: " + room.getSubjects());
        selectedDifficultiesTextView.setText("Difficulties: " + room.getDifficulties());
        String playersList = "Players in room:\n";
        for (int i = 0; i < room.getUsers().size(); i++) {
            playersList += room.getUsers().get(i).getUsername();
            if (i < room.getUsers().size() - 1) {
                playersList += "\n";
            }
        }
        playersListTextView.setText(playersList);
    }
}