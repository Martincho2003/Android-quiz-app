package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.viewModel.MultiplayerGameViewModel;

import java.util.List;

public class MultiplayerGameActivity extends AppCompatActivity {

    private static final String TAG = "MultiplayerGameActivity";
    private TextView questionTextView, timerTextView, pointsTextView;
    private Button answerButton1, answerButton2, answerButton3, answerButton4;
    private MultiplayerGameViewModel viewModel;
    private List<Question> currentQuestions;
    private Observer<List<Question>> questionsObserver;
    private Observer<Integer> questionIndexObserver;
    private Observer<Boolean> gameFinishedObserver;
    private Observer<Long> timerObserver;
    private Observer<Integer> pointsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        questionTextView = findViewById(R.id.questionTextView);
        timerTextView = findViewById(R.id.timerTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        answerButton4 = findViewById(R.id.answerButton4);

        Room room = (Room) getIntent().getSerializableExtra("room");
        if (room == null) {
            Log.e(TAG, "Room is null in MultiplayerGameActivity");
            Toast.makeText(this, "Error: Room not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Room received: " + room.getCreatorNickname() + ", questions: " + (room.getQuestions() != null ? room.getQuestions().size() : "null"));

        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new MultiplayerGameViewModel(room);
            }
        }).get(MultiplayerGameViewModel.class);

        questionTextView.setText("Loading questions...");
        answerButton1.setVisibility(View.GONE);
        answerButton2.setVisibility(View.GONE);
        answerButton3.setVisibility(View.GONE);
        answerButton4.setVisibility(View.GONE);

        questionsObserver = questions -> {
            if (questions == null || questions.isEmpty()) {
                Log.e(TAG, "No questions loaded");
                Toast.makeText(this, "Failed to load questions. Please try again.", Toast.LENGTH_LONG).show();
                questionTextView.setText("Failed to load questions. Go back and try again.");
                Button retryButton = new Button(this);
                retryButton.setText("Go Back");
                retryButton.setOnClickListener(v -> {
                    Intent mainIntent = new Intent(MultiplayerGameActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    cleanupObservers();
                    finish();
                });
                ((LinearLayout) questionTextView.getParent()).addView(retryButton);
                return;
            }
            currentQuestions = questions;
            updateQuestionUI();
            Log.d(TAG, "Questions loaded: " + questions.size());
        };
        viewModel.getCurrentQuestions().observe(this, questionsObserver);

        questionIndexObserver = index -> {
            if (currentQuestions == null || currentQuestions.isEmpty()) {
                Log.w(TAG, "Cannot update UI: No questions loaded");
                return;
            }
            if (index >= currentQuestions.size()) {
                Log.w(TAG, "Current index out of bounds: " + index);
                return;
            }
            updateQuestionUI();
        };
        viewModel.getCurrentQuestionIndex().observe(this, questionIndexObserver);

        gameFinishedObserver = finished -> {
            if (finished != null && finished) {
                Intent intent = new Intent(MultiplayerGameActivity.this, MultiplayerGameEndActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
                cleanupObservers();
                finish();
            }
        };
        viewModel.getGameFinished().observe(this, gameFinishedObserver);

        timerObserver = time -> {
            if (time != null) {
                timerTextView.setText("Time: " + time + "s");
            }
        };
        viewModel.getCurrentQuestionTime().observe(this, timerObserver);

        pointsObserver = points -> {
            if (points != null) {
                pointsTextView.setText("Points: " + points);
            }
        };
        viewModel.getPoints().observe(this, pointsObserver);

        answerButton1.setOnClickListener(v -> selectAnswer(0));
        answerButton2.setOnClickListener(v -> selectAnswer(1));
        answerButton3.setOnClickListener(v -> selectAnswer(2));
        answerButton4.setOnClickListener(v -> selectAnswer(3));
    }

    private void updateQuestionUI() {
        int currentIndex = viewModel.getCurrentQuestionIndex().getValue() != null ? viewModel.getCurrentQuestionIndex().getValue() : 0;
        if (currentIndex < currentQuestions.size()) {
            Question question = currentQuestions.get(currentIndex);
            questionTextView.setText(question.getQuestion());

            List<Answer> answers = question.getAnswers();
            answerButton1.setText(answers.size() > 0 ? answers.get(0).getAnswer() : "");
            answerButton2.setText(answers.size() > 1 ? answers.get(1).getAnswer() : "");
            answerButton3.setText(answers.size() > 2 ? answers.get(2).getAnswer() : "");
            answerButton4.setText(answers.size() > 3 ? answers.get(3).getAnswer() : "");

            answerButton1.setVisibility(answers.size() > 0 ? View.VISIBLE : View.GONE);
            answerButton2.setVisibility(answers.size() > 1 ? View.VISIBLE : View.GONE);
            answerButton3.setVisibility(answers.size() > 2 ? View.VISIBLE : View.GONE);
            answerButton4.setVisibility(answers.size() > 3 ? View.VISIBLE : View.GONE);
        }
    }

    private void selectAnswer(int answerIndex) {
        int currentIndex = viewModel.getCurrentQuestionIndex().getValue() != null ? viewModel.getCurrentQuestionIndex().getValue() : 0;
        if (currentIndex < currentQuestions.size()) {
            List<Answer> answers = currentQuestions.get(currentIndex).getAnswers();
            if (answerIndex < answers.size()) {
                viewModel.checkAnswer(answers.get(answerIndex));
            }
        }
    }

    private void cleanupObservers() {
        if (questionsObserver != null) {
            viewModel.getCurrentQuestions().removeObserver(questionsObserver);
        }
        if (questionIndexObserver != null) {
            viewModel.getCurrentQuestionIndex().removeObserver(questionIndexObserver);
        }
        if (gameFinishedObserver != null) {
            viewModel.getGameFinished().removeObserver(gameFinishedObserver);
        }
        if (timerObserver != null) {
            viewModel.getCurrentQuestionTime().removeObserver(timerObserver);
        }
        if (pointsObserver != null) {
            viewModel.getPoints().removeObserver(pointsObserver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupObservers();
    }
}