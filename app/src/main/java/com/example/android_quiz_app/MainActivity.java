package com.example.android_quiz_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Question;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView questionTextView;
    private TextView answersTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализиране на TextView-овете
        questionTextView = findViewById(R.id.questionTextView);
        answersTextView = findViewById(R.id.answersTextView);

        // Проверка дали TextView-овете са намерени
        if (questionTextView == null || answersTextView == null) {
            Log.e("MainActivity", "TextView не е намерен! Провери activity_main.xml");
            Toast.makeText(this, "Грешка: TextView не е намерен", Toast.LENGTH_LONG).show();
            return;
        }

        // Свързване с Firebase Realtime Database с правилния URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://android-quiz-app-8e645-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference databaseReference = database.getReference("questions/biology/easy");

        // Извличане на данните
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("MainActivity", "DataSnapshot: " + dataSnapshot.toString());
                Log.d("MainActivity", "Брой деца: " + dataSnapshot.getChildrenCount());

                // Проверка дали има данни
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    Log.e("MainActivity", "Няма данни на пътя questions/biology/easy");
                    Toast.makeText(MainActivity.this, "Няма данни в базата", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Извличане на първия въпрос от списъка
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    Log.d("MainActivity", "Question Snapshot: " + questionSnapshot.toString());
                    Question question = questionSnapshot.getValue(Question.class);

                    if (question != null) {
                        Log.d("MainActivity", "Въпрос: " + question.getQuestion());
                        Log.d("MainActivity", "Отговори: " + question.getAnswers());

                        // Проверка за null в отговорите
                        if (question.getAnswers() == null) {
                            Log.e("MainActivity", "Списъкът с отговори е null");
                            Toast.makeText(MainActivity.this, "Грешка: Няма отговори", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Показване на въпроса
                        questionTextView.setText(question.getQuestion());

                        // Показване на отговорите
                        StringBuilder answersText = new StringBuilder();
                        int correctAnswerIndex = -1;
                        for (int i = 0; i < question.getAnswers().size(); i++) {
                            Answer answer = question.getAnswers().get(i);
                            answersText.append(i).append(": ").append(answer.getAnswer());
                            if ("1".equals(answer.getIs_correct())) {
                                correctAnswerIndex = i;
                                answersText.append(" (верен)");
                            }
                            answersText.append("\n");
                        }
                        if (correctAnswerIndex != -1) {
                            answersText.append("Верен отговор: ").append(question.getAnswers().get(correctAnswerIndex).getAnswer());
                        } else {
                            answersText.append("Верен отговор: Не е посочен");
                        }
                        answersTextView.setText(answersText.toString());
                    } else {
                        Log.e("MainActivity", "Question обектът е null след десериализация");
                        Toast.makeText(MainActivity.this, "Няма данни", Toast.LENGTH_SHORT).show();
                    }
                    break; // Излизаме след първия въпрос за тест
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Firebase грешка: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Грешка: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}