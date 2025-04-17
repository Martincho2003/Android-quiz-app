package com.example.android_quiz_app.viewModel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.repository.GameService;
import com.example.android_quiz_app.repository.MultiplayerService;
import java.util.List;

public class JoinRoomViewModel extends ViewModel {
    private static final String TAG = "JoinRoomViewModel";
    private final GameService gameService;
    private final MultiplayerService multiplayerService;
    private final MutableLiveData<MultiplayerUser> currentUser;
    private final MutableLiveData<Room> joinedRoom;
    private final MutableLiveData<List<Room>> rooms;

    public JoinRoomViewModel() {
        gameService = new GameService();
        multiplayerService = MultiplayerService.getInstance();
        currentUser = new MutableLiveData<>();
        joinedRoom = new MutableLiveData<>();
        rooms = new MutableLiveData<>();

        gameService.getUserDetails().observeForever(user -> {
            if (user != null) {
                MultiplayerUser multiplayerUser = new MultiplayerUser(user.getUsername());
                currentUser.setValue(multiplayerUser);
                Log.d(TAG, "Current user loaded: " + multiplayerUser.getUsername());
            } else {
                Log.e(TAG, "Failed to load current user");
            }
        });

        multiplayerService.getRooms().observeForever(roomList -> {
            rooms.setValue(roomList);
            Room currentJoinedRoom = joinedRoom.getValue();
            if (currentJoinedRoom != null) {
                boolean found = false;
                for (Room room : roomList) {
                    if (room.getCreatorNickname().equals(currentJoinedRoom.getCreatorNickname())) {
                        Log.d(TAG, "Updating joined room: " + room.getCreatorNickname() + ", isGameStarted: " + room.isGameStarted());
                        joinedRoom.setValue(room);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Log.d(TAG, "Joined room no longer exists, resetting joinedRoom");
                    joinedRoom.setValue(null);
                }
            }
        });
    }

    public void joinRoom(Room room) {
        MultiplayerUser user = currentUser.getValue();
        if (user != null) {
            multiplayerService.joinRoom(room, user);
            joinedRoom.setValue(room);
            Log.d(TAG, "Joined room: " + room.getCreatorNickname());
        } else {
            Log.e(TAG, "Cannot join room: current user is null");
        }
    }

    public LiveData<MultiplayerUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Room> getJoinedRoom() {
        return joinedRoom;
    }

    public LiveData<List<Room>> getRooms() {
        return rooms;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Room room = joinedRoom.getValue();
        MultiplayerUser user = currentUser.getValue();
        if (room != null && user != null && !room.isGameStarted()) {
            multiplayerService.leaveRoom(room, user);
            Log.d(TAG, "Left room on cleanup: " + room.getCreatorNickname());
        }
    }
}