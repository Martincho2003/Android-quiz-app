package com.example.android_quiz_app.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiplayerService {
    private static MultiplayerService instance;
    private final DatabaseReference roomsRef;
    private final MutableLiveData<List<Room>> roomsLiveData;

    private MultiplayerService() {
        roomsRef = FirebaseManager.getInstance().getRoomsReference();
        roomsLiveData = new MutableLiveData<>(new ArrayList<>());

        // Слушаме за промени в стаите в реално време
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Room> rooms = new ArrayList<>();
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    Room room = roomSnapshot.getValue(Room.class);
                    if (room != null) {
                        rooms.add(room);
                    }
                }
                roomsLiveData.setValue(rooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка на грешка
            }
        });
    }

    public static MultiplayerService getInstance() {
        if (instance == null) {
            instance = new MultiplayerService();
        }
        return instance;
    }

    public LiveData<List<Room>> getRooms() {
        return roomsLiveData;
    }

    public void createRoom(Room room) {
        String roomKey = room.getCreatorNickname();
        roomsRef.child(roomKey).setValue(room);
    }

    public void joinRoom(Room room, MultiplayerUser user) {
        room.addUser(user);
        updateRoom(room);
    }

    public void leaveRoom(Room room, MultiplayerUser user) {
        room.removeUser(user);
        if (room.getUsers().isEmpty()) {
            roomsRef.child(room.getCreatorNickname()).removeValue();
        } else {
            updateRoom(room);
        }
    }

    public void startGame(Room room) {
        room.setIsGameStarted(true);
        updateRoom(room);
    }

    public void updateRoom(Room room) {
        Map<String, Object> roomValues = room.toMap();
        roomsRef.child(room.getCreatorNickname()).updateChildren(roomValues);
    }

    public void updateUserPoints(Room room, MultiplayerUser user) {
        for (int i = 0; i < room.getUsers().size(); i++) {
            if (room.getUsers().get(i).getUsername().equals(user.getUsername())) {
                room.getUsers().set(i, user);
                break;
            }
        }
        updateRoom(room);
    }
}