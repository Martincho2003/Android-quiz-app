package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        roomsRecyclerView = findViewById(R.id.roomsRecyclerView);
        roomListLayout = findViewById(R.id.roomListLayout);
        waitingLayout = findViewById(R.id.waitingLayout);
        roomTitleTextView = findViewById(R.id.roomTitleTextView);
        playersTextView = findViewById(R.id.playersTextView);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(this::joinRoom);
        roomsRecyclerView.setAdapter(roomAdapter);

        viewModel = new ViewModelProvider(this).get(JoinRoomViewModel.class);

        viewModel.getRooms().observe(this, rooms -> {
            Log.d(TAG, "Rooms updated: " + (rooms != null ? rooms.size() : 0));
            List<Room> filteredRooms = new ArrayList<>();
            for (Room room : rooms) {
                if (!room.isGameStarted()) {
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

    private void switchToWaitingScreen(Room room) {
        roomListLayout.setVisibility(LinearLayout.GONE);
        waitingLayout.setVisibility(LinearLayout.VISIBLE);
        roomTitleTextView.setText("Room: " + room.getCreatorNickname());
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Players: " + playerCount + "/4");
    }
}