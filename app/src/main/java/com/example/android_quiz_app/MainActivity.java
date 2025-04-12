package com.example.android_quiz_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.activities.LoginActivity;
import com.example.android_quiz_app.factory.MainViewModelFactory;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.repository.QuizRepository;
import com.example.android_quiz_app.viewModel.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView questionTextView;
    private TextView answersTextView;
    private Button logoutButton;
    private MainViewModel viewModel;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализиране на Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Проверка дали потребителят е влязъл
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Инициализиране на TextView-овете и бутона
        questionTextView = findViewById(R.id.questionTextView);
        answersTextView = findViewById(R.id.answersTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Инициализиране на ViewModel
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(new QuizRepository()))
                .get(MainViewModel.class);

        // Наблюдение на данните от ViewModel
        viewModel.getQuestion().observe(this, question -> {
            if (question != null) {
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
            }
        });

        // Наблюдение на грешки
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Изход бутон
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Обработка на бутона "назад" с OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Излизане от приложението
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}