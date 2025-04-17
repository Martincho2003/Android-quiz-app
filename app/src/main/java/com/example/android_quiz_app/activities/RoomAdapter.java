package com.example.android_quiz_app.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomAdapter(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
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
        Room room = rooms.get(position);
        holder.roomNameTextView.setText(room.getCreatorNickname());

        String subjects = room.getSubjects().stream()
                .map(Subject::getValue)
                .collect(Collectors.joining(", "));
        String difficulties = room.getDifficulties().stream()
                .map(Difficulty::getValue)
                .collect(Collectors.joining(", "));
        String players = "Players: " + room.getUsers().size() + "/4";

        holder.roomDetailsTextView.setText("Subjects: " + subjects + " | Difficulties: " + difficulties + " | " + players);

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomNameTextView, roomDetailsTextView;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTextView = itemView.findViewById(R.id.roomNameTextView);
            roomDetailsTextView = itemView.findViewById(R.id.roomDetailsTextView);
        }
    }
}