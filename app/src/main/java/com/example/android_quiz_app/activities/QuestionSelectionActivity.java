package com.example.android_quiz_app.activities;

import android.content.Intent;
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

    private CheckBox allSubjectsCheckBox, allDifficultiesCheckBox;
    private LinearLayout subjectsContainer, difficultiesContainer;
    private Button startGameButton;
    private List<CheckBox> subjectCheckBoxes;
    private List<CheckBox> difficultyCheckBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_selection);

        // Инициализиране на UI елементите
        allSubjectsCheckBox = findViewById(R.id.allSubjectsCheckBox);
        allDifficultiesCheckBox = findViewById(R.id.allDifficultiesCheckBox);
        subjectsContainer = findViewById(R.id.subjectsContainer);
        difficultiesContainer = findViewById(R.id.difficultiesContainer);
        startGameButton = findViewById(R.id.startGameButton);

        // Инициализиране на списъци за чекбоксове
        subjectCheckBoxes = new ArrayList<>();
        difficultyCheckBoxes = new ArrayList<>();

        // Генериране на чекбоксове за предмети
        for (Subject subject : Subject.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subject.name());
            checkBox.setPadding(0, 8, 0, 8);
            subjectsContainer.addView(checkBox);
            subjectCheckBoxes.add(checkBox);
        }

        // Генериране на чекбоксове за трудности
        for (Difficulty difficulty : Difficulty.values()) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(difficulty.name());
            checkBox.setPadding(0, 8, 0, 8);
            difficultiesContainer.addView(checkBox);
            difficultyCheckBoxes.add(checkBox);
        }

        // Логика за "Всички" предмети
        allSubjectsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : subjectCheckBoxes) {
                checkBox.setChecked(isChecked);
            }
        });

        // Логика за "Всички" трудности
        allDifficultiesCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox checkBox : difficultyCheckBoxes) {
                checkBox.setChecked(isChecked);
            }
        });

        // Старт на играта
        startGameButton.setOnClickListener(v -> {
            List<Subject> selectedSubjects = new ArrayList<>();
            List<Difficulty> selectedDifficulties = new ArrayList<>();

            // Проверка на избраните предмети
            for (int i = 0; i < subjectCheckBoxes.size(); i++) {
                if (subjectCheckBoxes.get(i).isChecked()) {
                    selectedSubjects.add(Subject.values()[i]);
                }
            }

            // Ако не е избран нито един предмет, избираме всички
            if (selectedSubjects.isEmpty()) {
                selectedSubjects.addAll(Arrays.asList(Subject.values()));
            }

            // Проверка на избраните трудности
            for (int i = 0; i < difficultyCheckBoxes.size(); i++) {
                if (difficultyCheckBoxes.get(i).isChecked()) {
                    selectedDifficulties.add(Difficulty.values()[i]);
                }
            }

            // Ако не е избрана нито една трудност, избираме всички
            if (selectedDifficulties.isEmpty()) {
                selectedDifficulties.addAll(Arrays.asList(Difficulty.values()));
            }

            // Стартиране на GameActivity с избраните параметри
            Intent intent = new Intent(QuestionSelectionActivity.this, GameActivity.class);
            intent.putExtra("subjects", new ArrayList<>(selectedSubjects));
            intent.putExtra("difficulties", new ArrayList<>(selectedDifficulties));
            startActivity(intent);
        });
    }
}