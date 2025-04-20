package com.example.android_quiz_app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.viewModel.LeaderboardViewModel;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;
    private List<User> topUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        topUsers = new ArrayList<>();
        adapter = new LeaderboardAdapter();
        leaderboardRecyclerView.setAdapter(adapter);

        viewModel.getLeaderboardState().observe(this, state -> {
            if (state.isSuccess()) {
                updateLeaderboard(state.getTopUsers());
            }
            Toast.makeText(LeaderboardActivity.this, state.getMessage(), Toast.LENGTH_LONG).show();
        });

        viewModel.getTop10Users();
    }

    private void updateLeaderboard(List<User> users) {
        topUsers.clear();
        topUsers.addAll(users);
        adapter.notifyDataSetChanged();
    }

    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = topUsers.get(position);
            int rank = position + 1;

            holder.rankTextView.setText(String.valueOf(rank));
            holder.usernameTextView.setText(user.getUsername());
            holder.pointsTextView.setText(user.getPoints() + " точки");
            if (position == 0) {
                holder.itemView.setBackgroundResource(R.drawable.rounded_top_corners);
                holder.medalImageView.setImageResource(R.drawable.gold_medal);
                holder.medalImageView.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundResource(R.color.gold);
            } else if (position == 1) {
                holder.medalImageView.setImageResource(R.drawable.silver_medal);
                holder.medalImageView.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundResource(R.color.silver);
            } else if (position == 2) {
                holder.medalImageView.setImageResource(R.drawable.bronze_medal);
                holder.medalImageView.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundResource(R.color.bronze);
            } else {
                if (position == topUsers.size() - 1){
                    holder.itemView.setBackgroundResource(R.drawable.rounded_bottom_corners);
                }else {
                    holder.itemView.setBackgroundResource(R.color.white);
                }
                holder.medalImageView.setImageResource(R.drawable.medal);
                holder.medalImageView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return topUsers.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankTextView;
            ImageView medalImageView;
            TextView usernameTextView;
            TextView pointsTextView;

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