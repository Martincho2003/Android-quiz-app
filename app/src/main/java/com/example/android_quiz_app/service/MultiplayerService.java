package com.example.android_quiz_app.service;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_quiz_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
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
    private final DatabaseReference usersRef;
    private final MutableLiveData<List<Room>> roomsLiveData;
    private final FirebaseAuth auth;

    private MultiplayerService() {
        roomsRef = FirebaseManager.getInstance().getRoomsReference();
        usersRef = FirebaseManager.getInstance().getUsersReference();
        this.auth = FirebaseAuth.getInstance();
        roomsLiveData = new MutableLiveData<>(new ArrayList<>());

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
                Log.e("MultiplayerService", "Failed to load rooms: " + error.getMessage());
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

    public void deleteRoom(Room room) {
        roomsRef.child(room.getCreatorNickname()).removeValue();
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
        roomsRef.child(room.getCreatorNickname()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                Room firebaseRoom = snapshot.getValue(Room.class);
                if (firebaseRoom != null) {
                    List<MultiplayerUser> users = firebaseRoom.getUsers();
                    if (users != null) {
                        for (int i = 0; i < users.size(); i++) {
                            if (users.get(i).getUsername().equals(user.getUsername())) {
                                users.set(i, user);
                                break;
                            }
                        }
                        firebaseRoom.setUsers(users);
                        updateRoom(firebaseRoom);
                        Log.d("MultiplayerService", "User points updated in Firebase for user: " + user.getUsername());
                    } else {
                        Log.e("MultiplayerService", "Users list is null in room: " + room.getCreatorNickname());
                    }
                } else {
                    Log.e("MultiplayerService", "Room not found in Firebase: " + room.getCreatorNickname());
                }
            } else {
                Log.e("MultiplayerService", "Failed to fetch room from Firebase: " + task.getException().getMessage());
            }
        });
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public LiveData<MultiplayerUser> getMultiplayerUserDetails() {
        MutableLiveData<MultiplayerUser> userDetailsLiveData = new MutableLiveData<>();

        String userId = getCurrentUserId();
        if (userId == null) {
            userDetailsLiveData.setValue(null);
            return userDetailsLiveData;
        }

        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                User userDetails = snapshot.getValue(User.class);
                if (userDetails != null) {
                    MultiplayerUser multiplayerUserDetails = new MultiplayerUser(userDetails.getUsername());
                    userDetailsLiveData.setValue(multiplayerUserDetails);
                } else {
                    userDetailsLiveData.setValue(null);
                }
            } else {
                userDetailsLiveData.setValue(null);
                Log.e("MultiplayerService", "Failed to fetch user details: " + task.getException().getMessage());
            }
        });

        return userDetailsLiveData;
    }
}