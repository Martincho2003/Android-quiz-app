package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Subject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionSelectionActivity extends AppCompatActivity {

    private LinearLayout subjectsContainer, difficultiesContainer;
    private Button startGameButton;
    private List<CheckBox> subjectCheckBoxes, difficultyCheckBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_selection);

        subjectsContainer = findViewById(R.id.subjectsContainer);
        difficultiesContainer = findViewById(R.id.difficultiesContainer);
        startGameButton = findViewById(R.id.startGameButton);

        subjectCheckBoxes = new ArrayList<>();
        difficultyCheckBoxes = new ArrayList<>();

        for (Subject subject : Subject.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subject.getValue());
            checkBox.setPadding(8, 8, 8, 8);
            checkBox.setTextSize(25);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setButtonDrawable(R.drawable.custom_checkbox);
            checkBox.setCompoundDrawablePadding(36);

            switch (subject) {
                case BIOLOGY:
                    checkBox.setText(R.string.subject_biology);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.biology, 0, 0, 0);
                    break;
                case HISTORY:
                    checkBox.setText(R.string.subject_history);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.history, 0, 0, 0);
                    break;
                case GEOGRAPHY:
                    checkBox.setText(R.string.subject_geography);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.geography, 0, 0, 0);
                    break;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
            checkBox.setLayoutParams(params);

            subjectsContainer.addView(checkBox);
            subjectCheckBoxes.add(checkBox);
        }

        for (Difficulty difficulty : Difficulty.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(difficulty.getValue());
            checkBox.setPadding(8, 8, 8, 8);
            checkBox.setTextSize(25);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setButtonDrawable(R.drawable.custom_checkbox);
            checkBox.setCompoundDrawablePadding(36);

            switch (difficulty) {
                case EASY:
                    checkBox.setText(R.string.difficulty_easy);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.angel, 0, 0, 0);
                    break;
                case HARD:
                    checkBox.setText(R.string.difficulty_hard);
                    checkBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.devil, 0, 0, 0);
                    break;
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp margin
            checkBox.setLayoutParams(params);

            difficultiesContainer.addView(checkBox);
            difficultyCheckBoxes.add(checkBox);
        }

        startGameButton.setOnClickListener(v -> {
            List<Subject> selectedSubjects = new ArrayList<>();
            List<Difficulty> selectedDifficulties = new ArrayList<>();
            for (int i = 0; i < subjectCheckBoxes.size(); i++) {
                if (subjectCheckBoxes.get(i).isChecked()) {
                    selectedSubjects.add(Subject.values()[i]);
                }
            }
            if (selectedSubjects.isEmpty()) {
                selectedSubjects.addAll(Arrays.asList(Subject.values()));
            }
            for (int i = 0; i < difficultyCheckBoxes.size(); i++) {
                if (difficultyCheckBoxes.get(i).isChecked()) {
                    selectedDifficulties.add(Difficulty.values()[i]);
                }
            }
            if (selectedDifficulties.isEmpty()) {
                selectedDifficulties.addAll(Arrays.asList(Difficulty.values()));
            }

            Intent intent = new Intent(QuestionSelectionActivity.this, GameActivity.class);
            intent.putExtra("subjects", new ArrayList<>(selectedSubjects));
            intent.putExtra("difficulties", new ArrayList<>(selectedDifficulties));
            startActivity(intent);
        });
    }
}