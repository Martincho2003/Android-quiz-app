package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.service.MultiplayerService;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerGameEndActivity extends AppCompatActivity {

    private static final String TAG = "MultiplayerGameEndActivity";
    private RecyclerView leaderboardRecyclerView;
    private Button backToMainButton;
    private Room room;
    private MultiplayerService multiplayerService;
    private LeaderboardAdapter leaderboardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game_end);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        backToMainButton = findViewById(R.id.backToMainButton);

        room = (Room) getIntent().getSerializableExtra("room");
        if (room == null) {
            Log.e(TAG, "Room is null in MultiplayerGameEndActivity");
            finish();
            return;
        }
        Log.d(TAG, "Room received: " + room.getCreatorNickname());

        multiplayerService = MultiplayerService.getInstance();

        // Setup RecyclerView
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter();
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        // Observe room updates
        multiplayerService.getRooms().observe(this, rooms -> {
            for (Room updatedRoom : rooms) {
                if (updatedRoom.getCreatorNickname().equals(room.getCreatorNickname())) {
                    room = updatedRoom;
                    updateLeaderboard();
                    break;
                }
            }
        });

        backToMainButton.setOnClickListener(v -> onBackToMainClicked());

        updateLeaderboard();
    }

    private void updateLeaderboard() {
        if (room.getUsers() != null) {
            List<MultiplayerUser> sortedUsers = new ArrayList<>(room.getUsers());
            sortedUsers.sort((u1, u2) -> Integer.compare(u2.getGamePoints(), u1.getGamePoints()));
            leaderboardAdapter.updateUsers(sortedUsers);
        }
    }

    public void onBackToMainClicked() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        private List<MultiplayerUser> users = new ArrayList<>();

        public void updateUsers(List<MultiplayerUser> newUsers) {
            this.users.clear();
            this.users.addAll(newUsers);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MultiplayerUser user = users.get(position);
            holder.rankTextView.setText(String.valueOf(position + 1));
            holder.usernameTextView.setText(user.getUsername());
            holder.pointsTextView.setText(getString(R.string.points_format, user.getGamePoints()));

            if (position == 0 || position == getItemCount() - 1) {
                holder.itemView.setBackgroundResource(R.drawable.leaderboard_item_background);
            } else {
                holder.itemView.setBackgroundResource(android.R.color.white);
            }

            switch (position) {
                case 0:
                    holder.rankTextView.setTextColor(getResources().getColor(R.color.gold));
                    holder.usernameTextView.setTextColor(getResources().getColor(R.color.gold));
                    holder.pointsTextView.setTextColor(getResources().getColor(R.color.gold));
                    holder.medalImageView.setImageResource(R.drawable.gold_medal);
                    holder.medalImageView.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    holder.rankTextView.setTextColor(getResources().getColor(R.color.silver));
                    holder.usernameTextView.setTextColor(getResources().getColor(R.color.silver));
                    holder.pointsTextView.setTextColor(getResources().getColor(R.color.silver));
                    holder.medalImageView.setImageResource(R.drawable.silver_medal);
                    holder.medalImageView.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.rankTextView.setTextColor(getResources().getColor(R.color.bronze));
                    holder.usernameTextView.setTextColor(getResources().getColor(R.color.bronze));
                    holder.pointsTextView.setTextColor(getResources().getColor(R.color.bronze));
                    holder.medalImageView.setImageResource(R.drawable.bronze_medal);
                    holder.medalImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.rankTextView.setTextColor(getResources().getColor(android.R.color.black));
                    holder.usernameTextView.setTextColor(getResources().getColor(android.R.color.black));
                    holder.pointsTextView.setTextColor(getResources().getColor(android.R.color.black));
                    holder.medalImageView.setImageResource(R.drawable.medal);
                    holder.medalImageView.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankTextView, usernameTextView, pointsTextView;
            ImageView medalImageView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                rankTextView = itemView.findViewById(R.id.rankTextView);
                medalImageView = itemView.findViewById(R.id.medalImageView);
                usernameTextView = itemView.findViewById(R.id.usernameTextView);
                pointsTextView = itemView.findViewById(R.id.pointsTextView);
            }
        }
    }
}