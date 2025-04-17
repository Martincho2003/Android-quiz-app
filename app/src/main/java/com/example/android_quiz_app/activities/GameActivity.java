package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_quiz_app.MainActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.factory.GameViewModelFactory;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.viewModel.GameViewModel;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private TextView questionTextView, timerTextView, pointsTextView;
    private Button answerButton1, answerButton2, answerButton3, answerButton4, addTimeButton, excludeButton;
    private GameViewModel viewModel;
    private List<Question> currentQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        questionTextView = findViewById(R.id.questionTextView);
        timerTextView = findViewById(R.id.timerTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        answerButton4 = findViewById(R.id.answerButton4);
        addTimeButton = findViewById(R.id.addTimeButton);
        excludeButton = findViewById(R.id.excludeButton);

        Intent intent = getIntent();
        ArrayList<Subject> subjects = (ArrayList<Subject>) intent.getSerializableExtra("subjects");
        ArrayList<Difficulty> difficulties = (ArrayList<Difficulty>) intent.getSerializableExtra("difficulties");

        Log.d(TAG, "Subjects: " + subjects.toString());
        Log.d(TAG, "Difficulties: " + difficulties.toString());

        GameViewModelFactory factory = new GameViewModelFactory(subjects, difficulties);
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);

        questionTextView.setText("Loading questions...");
        answerButton1.setVisibility(Button.GONE);
        answerButton2.setVisibility(Button.GONE);
        answerButton3.setVisibility(Button.GONE);
        answerButton4.setVisibility(Button.GONE);
        addTimeButton.setEnabled(false);
        excludeButton.setEnabled(false);

        viewModel.getQuestions().observe(this, questions -> {
            if (questions == null || questions.isEmpty()) {
                Log.e(TAG, "No questions loaded");
                Toast.makeText(this, "Failed to load questions. Please try again.", Toast.LENGTH_LONG).show();
                questionTextView.setText("Failed to load questions. Go back and try again.");
                Button retryButton = new Button(this);
                retryButton.setText("Go Back");
                retryButton.setOnClickListener(v -> {
                    Intent mainIntent = new Intent(GameActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                });
                ((LinearLayout) questionTextView.getParent()).addView(retryButton);
                return;
            }
            currentQuestions = questions;
            updateQuestionUI();
            Log.d(TAG, "Questions loaded: " + questions.size());
        });

        viewModel.getCurrentQuestionIndex().observe(this, index -> {
            if (currentQuestions == null || currentQuestions.isEmpty()) {
                Log.w(TAG, "Cannot update UI: No questions loaded");
                return;
            }
            if (index >= currentQuestions.size()) {
                Log.w(TAG, "Current index out of bounds: " + index);
                return;
            }
            updateQuestionUI();
        });

        viewModel.getGameEnded().observe(this, finalPoints -> {
            if (finalPoints != null) {
                Log.d(TAG, "Game ended, showing dialog with points: " + finalPoints);
                showGameOverDialog(finalPoints);
            }
        });

        viewModel.getCurrentQuestionTime().observe(this, time -> {
            timerTextView.setText("Time: " + time + "s");
        });

        viewModel.getPoints().observe(this, points -> {
            pointsTextView.setText("Points: " + points);
            if (currentQuestions != null && !currentQuestions.isEmpty()) {
                updateButtonsState();
            }
        });

        viewModel.getUserDetails().observe(this, user -> {
            if (user == null) {
                Log.w(TAG, "User details not loaded yet");
                return;
            }
            Log.d(TAG, "User details loaded: " + user.getUsername() + ", points: " + user.getPoints());
            updateButtonsState();
        });

        viewModel.getIsAddTime().observe(this, addTimeList -> {
            if (currentQuestions != null && !currentQuestions.isEmpty()) {
                updateButtonsState();
            }
        });

        viewModel.getIsExclude().observe(this, excludeList -> {
            if (currentQuestions != null && !currentQuestions.isEmpty()) {
                updateButtonsState();
            }
        });

        answerButton1.setOnClickListener(v -> selectAnswer(0));
        answerButton2.setOnClickListener(v -> selectAnswer(1));
        answerButton3.setOnClickListener(v -> selectAnswer(2));
        answerButton4.setOnClickListener(v -> selectAnswer(3));

        addTimeButton.setOnClickListener(v -> viewModel.addTime());
        excludeButton.setOnClickListener(v -> viewModel.excludeAnswers());
    }

    private void showGameOverDialog(int finalPoints) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Ти спечели " + finalPoints + " точки!")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent mainIntent = new Intent(GameActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                })
                .setCancelable(false)
                .show();
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

            answerButton1.setVisibility(answers.size() > 0 ? Button.VISIBLE : Button.GONE);
            answerButton2.setVisibility(answers.size() > 1 ? Button.VISIBLE : Button.GONE);
            answerButton3.setVisibility(answers.size() > 2 ? Button.VISIBLE : Button.GONE);
            answerButton4.setVisibility(answers.size() > 3 ? Button.VISIBLE : Button.GONE);

            updateButtonsState();
        }
    }

    private void updateButtonsState() {
        if (currentQuestions == null || currentQuestions.isEmpty()) {
            Log.w(TAG, "Cannot update buttons: No questions loaded");
            addTimeButton.setEnabled(false);
            excludeButton.setEnabled(false);
            return;
        }
        addTimeButton.setEnabled(!viewModel.isAddTimeDeactivated());
        excludeButton.setEnabled(!viewModel.isExcludeDeactivated());
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
}