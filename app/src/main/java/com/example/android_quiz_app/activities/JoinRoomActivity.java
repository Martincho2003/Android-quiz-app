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

import com.airbnb.lottie.LottieAnimationView;
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
    private LottieAnimationView joinAnimation, waitingAnimation1, waitingAnimation2, waitingAnimation3, waitingAnimation4;
    private LottieAnimationView questionMarkAnimation1, questionMarkAnimation2, questionMarkAnimation3;

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
        questionMarkAnimation1 = findViewById(R.id.questionMarkAnimation1);
        questionMarkAnimation2 = findViewById(R.id.questionMarkAnimation2);
        questionMarkAnimation3 = findViewById(R.id.questionMarkAnimation3);

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

        viewModel.joinRoom(room);
        Toast.makeText(this, "Присъединихте се към стаята: " + room.getCreatorNickname(), Toast.LENGTH_SHORT).show();
    }

    private void switchToWaitingScreen(Room room) {
        roomListLayout.setVisibility(LinearLayout.GONE);
        waitingLayout.setVisibility(LinearLayout.VISIBLE);
        joinAnimation.setVisibility(LinearLayout.GONE); // Скриване на join анимацията
        roomTitleTextView.setText("Стая: " + room.getCreatorNickname());

        // Показване и стартиране на waiting анимациите
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

        // Показване и стартиране на questionMark анимациите
        questionMarkAnimation1.setVisibility(LinearLayout.VISIBLE);
        questionMarkAnimation2.setVisibility(LinearLayout.VISIBLE);
        questionMarkAnimation3.setVisibility(LinearLayout.VISIBLE);

        questionMarkAnimation1.setAnimation(R.raw.question_marks);
        questionMarkAnimation2.setAnimation(R.raw.question_marks);
        questionMarkAnimation3.setAnimation(R.raw.question_marks);

        questionMarkAnimation1.playAnimation();
        questionMarkAnimation2.playAnimation();
        questionMarkAnimation3.playAnimation();

        // Лека анимация за текстовете
        waitingLayout.setAlpha(0f);
        waitingLayout.animate()
                .alpha(1f)
                .setDuration(1000)
                .start();
    }

    private void updateWaitingScreen(Room room) {
        int playerCount = room.getUsers().size();
        playersTextView.setText("Играчи: " + playerCount + "/4");
    }
}