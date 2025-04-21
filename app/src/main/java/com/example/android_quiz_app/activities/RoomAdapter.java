package com.example.android_quiz_app.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.model.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> rooms = new ArrayList<>();
    private final OnRoomClickListener listener;
    private final Context context;
    private List<Room> filteredRooms = new ArrayList<>();

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomAdapter(Context context, OnRoomClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        this.filteredRooms = new ArrayList<>(this.rooms);
        notifyDataSetChanged();
    }

    public void filterRooms(String query) {
        filteredRooms.clear();
        if (query.isEmpty()) {
            filteredRooms.addAll(rooms);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Room room : rooms) {
                if (room.getCreatorNickname().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRooms.add(room);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = filteredRooms.get(position);
        holder.roomNameTextView.setText(room.getCreatorNickname());

        String subjects = room.getSubjects().stream()
                .map(subject -> {
                    switch (subject.name()) {
                        case "BIOLOGY":
                            return context.getString(R.string.subject_biology);
                        case "HISTORY":
                            return context.getString(R.string.subject_history);
                        case "GEOGRAPHY":
                            return context.getString(R.string.subject_geography);
                        default:
                            return subject.getValue();
                    }
                })
                .collect(Collectors.joining(", "));

        String difficulties = room.getDifficulties().stream()
                .map(difficulty -> {
                    switch (difficulty.name()) {
                        case "EASY":
                            return context.getString(R.string.difficulty_easy);
                        case "HARD":
                            return context.getString(R.string.difficulty_hard);
                        default:
                            return difficulty.getValue();
                    }
                })
                .collect(Collectors.joining(", "));

        String players = context.getString(R.string.players, room.getUsers().size());

        String details = context.getString(R.string.room_details, subjects, difficulties, players);
        holder.roomDetailsTextView.setText(details);

        String contentDescription = context.getString(R.string.room_content_description,
                room.getCreatorNickname(), subjects, difficulties, room.getUsers().size());
        holder.itemView.setContentDescription(contentDescription);

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() {
        return filteredRooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomNameTextView, roomDetailsTextView;
        ImageView roomIconImageView;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTextView = itemView.findViewById(R.id.roomNameTextView);
            roomDetailsTextView = itemView.findViewById(R.id.roomDetailsTextView);
            roomIconImageView = itemView.findViewById(R.id.roomIconImageView);
        }
    }
}