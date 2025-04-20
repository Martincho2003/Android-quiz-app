package com.example.android_quiz_app.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.service.FirebaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardViewModel extends ViewModel {

    private DatabaseReference usersReference;

    private final MutableLiveData<LeaderboardState> leaderboardState = new MutableLiveData<>();

    public LeaderboardViewModel() { usersReference = FirebaseManager.getInstance().getUsersReference(); }

    public LiveData<LeaderboardState> getLeaderboardState() { return leaderboardState; }

    public void getTop10Users() {
        Query topUsersQuery = usersReference.orderByChild("points").limitToLast(10);

        topUsersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> topUsers = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        topUsers.add(user);
                    }
                }
                Collections.reverse(topUsers);

                leaderboardState.setValue(new LeaderboardState(true, "Класацията е успешно заредена.", topUsers));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                leaderboardState.setValue(new LeaderboardState(false, "Неуспешно зареждане на класацията: " + databaseError.getMessage(), null));
            }
        });
    }

    public static class LeaderboardState {
        private final boolean isSuccess;
        private final String message;
        private final List<User> topUsers;

        public LeaderboardState(boolean isSuccess, String message, List<User> topUsers) {
            this.isSuccess = isSuccess;
            this.message = message;
            this.topUsers = topUsers;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public List<User> getTopUsers() {
            return topUsers;
        }
    }
}