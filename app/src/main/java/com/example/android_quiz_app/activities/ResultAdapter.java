package com.example.android_quiz_app.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.User;
import java.util.ArrayList;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

    private List<MultiplayerUser> users = new ArrayList<>();

    public void setUsers(List<MultiplayerUser> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        MultiplayerUser user = users.get(position);
        holder.playerNameTextView.setText(user.getUsername());
        holder.playerPointsTextView.setText(user.getGamePoints() + " points");
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView playerNameTextView, playerPointsTextView;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            playerPointsTextView = itemView.findViewById(R.id.playerPointsTextView);
        }
    }
}