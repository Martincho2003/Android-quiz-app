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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private TextView questionTextView;
    private Button option1Button, option2Button, option3Button, option4Button;
    private Button backButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private static final String TAG = "GameActivity";
    private static final int MAX_GAMES_PER_DAY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Инициализиране на Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Инициализиране на UI елементите
        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        backButton = findViewById(R.id.backButton);

        // Проверка дали потребителят е влязъл
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to play", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        checkUserGameLimit(userId);

        // Примерен въпрос (можеш да добавиш логика за зареждане на въпроси)
        questionTextView.setText("What is the capital of France?");
        option1Button.setText("Paris");
        option2Button.setText("London");
        option3Button.setText("Berlin");
        option4Button.setText("Madrid");

        // Логика за избор на отговор (пример)
        option1Button.setOnClickListener(v -> {
            Toast.makeText(GameActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
            updateUserStats(userId, 10); // Добавяме 10 точки за правилен отговор
        });

        option2Button.setOnClickListener(v -> {
            Toast.makeText(GameActivity.this, "Wrong!", Toast.LENGTH_SHORT).show();
            updateUserStats(userId, 0); // Без точки за грешен отговор
        });

        option3Button.setOnClickListener(v -> {
            Toast.makeText(GameActivity.this, "Wrong!", Toast.LENGTH_SHORT).show();
            updateUserStats(userId, 0);
        });

        option4Button.setOnClickListener(v -> {
            Toast.makeText(GameActivity.this, "Wrong!", Toast.LENGTH_SHORT).show();
            updateUserStats(userId, 0);
        });

        // Бутон за връщане
        backButton.setOnClickListener(v -> finish());
    }

    private void checkUserGameLimit(String userId) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    Log.e(TAG, "User data not found in database");
                    Toast.makeText(GameActivity.this, "User data not found, please register again", Toast.LENGTH_LONG).show();
                    auth.signOut();
                    finish();
                    return;
                }

                String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                String lastDayPlayed = user.getLastDayPlayed();
                int playedGamesToday = user.getPlayedGamesToday();

                // Проверка дали е нов ден
                if (lastDayPlayed == null || !lastDayPlayed.equals(currentDate)) {
                    Log.d(TAG, "New day detected, resetting playedGamesToday");
                    user.setLastDayPlayed(currentDate);
                    user.setPlayedGamesToday(0);
                    playedGamesToday = 0;
                }

                // Проверка на лимита за игри
                if (playedGamesToday >= MAX_GAMES_PER_DAY) {
                    Toast.makeText(GameActivity.this, "You have reached the daily game limit (" + MAX_GAMES_PER_DAY + " games)", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Ако всичко е наред, позволяваме играта
                Log.d(TAG, "User can play, games today: " + playedGamesToday);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user data: " + databaseError.getMessage());
                Toast.makeText(GameActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void updateUserStats(String userId, int pointsToAdd) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    Log.e(TAG, "User data not found in database");
                    Toast.makeText(GameActivity.this, "User data not found, please register again", Toast.LENGTH_LONG).show();
                    auth.signOut();
                    finish();
                    return;
                }

                String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                String lastDayPlayed = user.getLastDayPlayed();
                int playedGamesToday = user.getPlayedGamesToday();

                // Проверка дали е нов ден
                if (lastDayPlayed == null || !lastDayPlayed.equals(currentDate)) {
                    Log.d(TAG, "New day detected, resetting playedGamesToday");
                    user.setLastDayPlayed(currentDate);
                    user.setPlayedGamesToday(0);
                    playedGamesToday = 0;
                }

                // Обновяване на статистиката
                user.setPoints(user.getPoints() + pointsToAdd);
                user.setPlayedGamesToday(playedGamesToday + 1);

                // Запазване на обновените данни
                databaseReference.child(userId).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User stats updated successfully");
                                Toast.makeText(GameActivity.this, "Game finished! Points: " + user.getPoints(), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e(TAG, "Failed to update user stats: " + task.getException().getMessage());
                                Toast.makeText(GameActivity.this, "Failed to update stats: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user data: " + databaseError.getMessage());
                Toast.makeText(GameActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}