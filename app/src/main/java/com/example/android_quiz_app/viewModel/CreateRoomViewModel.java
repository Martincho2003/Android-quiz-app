package com.example.android_quiz_app.viewModel;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.service.GameService;
import com.example.android_quiz_app.service.MultiplayerService;
import java.util.List;

public class CreateRoomViewModel extends ViewModel {
    private static final String TAG = "CreateRoomViewModel";
    private final GameService gameService;
    private final MultiplayerService multiplayerService;
    private final MutableLiveData<MultiplayerUser> currentUser;
    private final MutableLiveData<Room> createdRoom;
    private final MutableLiveData<List<Question>> loadedQuestions;
    private final MutableLiveData<Boolean> roomCreationFailed;

    public CreateRoomViewModel() {
        gameService = new GameService();
        multiplayerService = MultiplayerService.getInstance();
        currentUser = new MutableLiveData<>();
        createdRoom = new MutableLiveData<>();
        loadedQuestions = new MutableLiveData<>();
        roomCreationFailed = new MutableLiveData<>(false);

        gameService.getUserDetails().observeForever(user -> {
            if (user != null) {
                MultiplayerUser multiplayerUser = new MultiplayerUser(user.getUsername());
                currentUser.setValue(multiplayerUser);
                Log.d(TAG, "Current user loaded: " + multiplayerUser.getUsername());
            }
        });

        multiplayerService.getRooms().observeForever(rooms -> {
            Room currentRoom = createdRoom.getValue();
            if (currentRoom != null) {
                boolean found = false;
                for (Room room : rooms) {
                    if (room.getCreatorNickname().equals(currentRoom.getCreatorNickname())) {
                        Log.d(TAG, "Found updated room: " + room.getCreatorNickname() +
                                ", players: " + room.getUsers().size());
                        Room updatedRoom = new Room(room.getCreatorNickname(), room.getSubjects(), room.getDifficulties());
                        updatedRoom.setUsers(room.getUsers());
                        updatedRoom.setQuestions(room.getQuestions());
                        updatedRoom.setIsGameStarted(room.isGameStarted());
                        createdRoom.setValue(updatedRoom);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Log.d(TAG, "Room no longer exists, resetting createdRoom");
                    createdRoom.setValue(null);
                }
            } else {
                Log.d(TAG, "Current room is null, skipping update");
            }
        });
    }

    public void createRoom(List<Subject> subjects, List<Difficulty> difficulties, LifecycleOwner owner) {
        if (currentUser.getValue() == null) {
            Log.d(TAG, "User not loaded yet, waiting...");
            currentUser.observe(owner, user -> {
                if (user != null) {
                    createRoomInitial(subjects, difficulties, user);
                }
            });
        } else {
            MultiplayerUser user = currentUser.getValue();
            createRoomInitial(subjects, difficulties, user);
        }
    }

    private void createRoomInitial(List<Subject> subjects, List<Difficulty> difficulties, MultiplayerUser user) {
        Room room = new Room(user.getUsername(), subjects, difficulties);
        room.addUser(user);
        multiplayerService.createRoom(room);
        createdRoom.setValue(room);

        gameService.getQuestionsFromPub(difficulties, subjects).observeForever(questions -> {
            if (questions != null && !questions.isEmpty()) {
                room.setQuestions(questions);
                multiplayerService.updateRoom(room);
                loadedQuestions.setValue(questions);
                Log.d(TAG, "Questions loaded for room: " + questions.size());
            } else {
                multiplayerService.leaveRoom(room, user);
                roomCreationFailed.setValue(true);
            }
        });
    }

    public void deleteRoom(Room room) {
        if (room != null) {
            multiplayerService.deleteRoom(room);
            createdRoom.setValue(null);
            Log.d(TAG, "Room deleted: " + room.getCreatorNickname());
        }
    }

    public void startGame() {
        Room room = createdRoom.getValue();
        if (room != null) {
            multiplayerService.startGame(room);
        }
    }

    public LiveData<MultiplayerUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Room> getCreatedRoom() {
        return createdRoom;
    }

    public LiveData<List<Question>> getLoadedQuestions() {
        return loadedQuestions;
    }

    public LiveData<Boolean> getRoomCreationFailed() {
        return roomCreationFailed;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Room room = createdRoom.getValue();
        MultiplayerUser user = currentUser.getValue();
        if (room != null && user != null && !room.isGameStarted()) {
            multiplayerService.deleteRoom(room);
            Log.d(TAG, "Room deleted on cleanup: " + room.getCreatorNickname());
        }
    }
}