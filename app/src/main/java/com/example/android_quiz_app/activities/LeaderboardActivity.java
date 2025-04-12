package com.example.android_quiz_app.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private TextView leaderboardTextView;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Инициализиране на Firebase Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Инициализиране на TextView
        leaderboardTextView = findViewById(R.id.leaderboardTextView);

        // Зареждане на класацията
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Сортиране на потребителите по точки (намаляващ ред)
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return Integer.compare(u2.getPoints(), u1.getPoints());
                    }
                });

                // Показване на класацията
                StringBuilder leaderboardText = new StringBuilder();
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);
                    leaderboardText.append(i + 1).append(". ")
                            .append(user.getUsername())
                            .append(": ")
                            .append(user.getPoints())
                            .append(" points\n");
                }
                leaderboardTextView.setText(leaderboardText.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}