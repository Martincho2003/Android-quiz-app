package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, pointsTextView, lastDayPlayedTextView, playedGamesTodayTextView;
    private Button logoutButton, backButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Инициализиране на Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Инициализиране на UI елементите
        usernameTextView = findViewById(R.id.usernameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        lastDayPlayedTextView = findViewById(R.id.lastDayPlayedTextView);
        playedGamesTodayTextView = findViewById(R.id.playedGamesTodayTextView);
        logoutButton = findViewById(R.id.logoutButton);
        backButton = findViewById(R.id.backButton);

        // Проверка дали потребителят е влязъл
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        loadUserProfile(userId);

        // Бутон за изход
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Бутон за връщане
        backButton.setOnClickListener(v -> finish());
    }

    private void loadUserProfile(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    Log.e(TAG, "User data not found in database");
                    Toast.makeText(ProfileActivity.this, "User data not found, please register again", Toast.LENGTH_LONG).show();
                    auth.signOut();
                    finish();
                    return;
                }

                // Показване на данните на потребителя
                usernameTextView.setText("Username: " + user.getUsername());
                pointsTextView.setText("Points: " + user.getPoints());
                lastDayPlayedTextView.setText("Last played: " + (user.getLastDayPlayed() != null ? user.getLastDayPlayed() : "Never"));
                playedGamesTodayTextView.setText("Games today: " + user.getPlayedGamesToday());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user data: " + databaseError.getMessage());
                Toast.makeText(ProfileActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}