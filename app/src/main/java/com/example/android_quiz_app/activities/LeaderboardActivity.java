package com.example.android_quiz_app.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.viewModel.LeaderboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView leaderboardListView;
    private LeaderboardViewModel viewModel;
    private ArrayAdapter<String> adapter;
    private List<String> leaderboardEntries;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        leaderboardListView = findViewById(R.id.leaderboardListView);
        leaderboardEntries = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardEntries);
        leaderboardListView.setAdapter(adapter);

        viewModel.getLeaderboardState().observe(this, state -> {
            if (state.isSuccess()) {
                updateLeaderboard(state.getTopUsers());
            }
            Toast.makeText(LeaderboardActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
        });

        viewModel.getTop10Users();
    }

    private void updateLeaderboard(List<User> topUsers) {
        leaderboardEntries.clear();
        int rank = 1;
        for (User user : topUsers) {
            String entry = rank + ". " + user.getUsername() + " - " + user.getPoints() + " points";
            leaderboardEntries.add(entry);
            rank++;
        }
        adapter.notifyDataSetChanged();
    }
}