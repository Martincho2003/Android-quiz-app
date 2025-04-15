package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.viewModel.MultiplayerGameViewModel;

import java.util.List;

public class MultiplayerGameActivity extends AppCompatActivity {

    private static final String TAG = "MultiplayerGameActivity";
    private TextView questionNumberTextView, timerTextView, questionTextView, resultsTextView;
    private RadioGroup answerRadioGroup;
    private Button submitAnswerButton, backToMenuButton;
    private LinearLayout questionLayout, resultsLayout;
    private MultiplayerGameViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        // Инициализиране на UI елементите
        questionNumberTextView = findViewById(R.id.questionNumberTextView);
        timerTextView = findViewById(R.id.timerTextView);
        questionTextView = findViewById(R.id.questionTextView);
        answerRadioGroup = findViewById(R.id.answerRadioGroup);
        submitAnswerButton = findViewById(R.id.submitAnswerButton);
        questionLayout = findViewById(R.id.questionLayout);
        resultsLayout = findViewById(R.id.resultsLayout);
        resultsTextView = findViewById(R.id.resultsTextView);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        // Вземане на Room обекта от интента
        Room room = (Room) getIntent().getSerializableExtra("room");
        if (room == null) {
            Toast.makeText(this, "Error: Room not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Инициализиране на ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new MultiplayerGameViewModel(room);
            }
        }).get(MultiplayerGameViewModel.class);

        // Наблюдение на LiveData обектите от ViewModel
        viewModel.getCurrentQuestion().observe(this, this::updateQuestionUI);
        viewModel.getCurrentQuestionIndex().observe(this, index ->
                questionNumberTextView.setText("Question " + (index + 1) + "/" + viewModel.getTotalQuestions()));
        viewModel.getTimeLeft().observe(this, timeLeft ->
                timerTextView.setText("Time left: " + (timeLeft / 1000) + "s"));
        viewModel.getGameFinished().observe(this, finished -> {
            if (finished != null && finished) {
                showResults();
            }
        });

        // Обработка на бутона за подаване на отговор
        submitAnswerButton.setOnClickListener(v -> {
            int selectedId = answerRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selectedButton = findViewById(selectedId);
            int selectedAnswerIndex = answerRadioGroup.indexOfChild(selectedButton);
            viewModel.submitAnswer(selectedAnswerIndex);
        });

        // Обработка на бутона за връщане към менюто
        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(MultiplayerGameActivity.this, GameModeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void updateQuestionUI(Question question) {
        if (question == null) {
            Toast.makeText(this, "Error: Question not found", Toast.LENGTH_SHORT).show();
            return;
        }

        questionTextView.setText(question.getQuestion());
        answerRadioGroup.removeAllViews();

        for (Answer answer : question.getAnswers()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(answer.getAnswer());
            answerRadioGroup.addView(radioButton);
        }
    }

    private void showResults() {
        questionLayout.setVisibility(LinearLayout.GONE);
        resultsLayout.setVisibility(LinearLayout.VISIBLE);

        Room room = viewModel.getRoom().getValue();
        if (room == null) {
            resultsTextView.setText("Error: Room data not available");
            return;
        }

        StringBuilder results = new StringBuilder();
        List<MultiplayerUser> users = room.getUsers();
        users.sort((u1, u2) -> Integer.compare(u2.getGamePoints(), u1.getGamePoints())); // Сортираме по точки

        for (MultiplayerUser user : users) {
            results.append(user.getUsername()).append(": ").append(user.getGamePoints()).append(" points\n");
        }

        resultsTextView.setText(results.toString());
    }
}