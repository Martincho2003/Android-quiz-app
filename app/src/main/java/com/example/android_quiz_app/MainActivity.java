package com.example.android_quiz_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_quiz_app.factory.MainViewModelFactory;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.repository.QuizRepository;
import com.example.android_quiz_app.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private TextView questionTextView;
    private TextView answersTextView;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionTextView = findViewById(R.id.questionTextView);
        answersTextView = findViewById(R.id.answersTextView);

        viewModel = new ViewModelProvider(this, new MainViewModelFactory(new QuizRepository()))
                .get(MainViewModel.class);

        viewModel.getQuestion().observe(this, question -> {
            if (question != null) {
                questionTextView.setText(question.getQuestion());

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

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}