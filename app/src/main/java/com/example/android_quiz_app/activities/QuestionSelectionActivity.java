package com.example.android_quiz_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_quiz_app.R;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Subject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionSelectionActivity extends AppCompatActivity {

    private CheckBox biologyCheckBox, historyCheckBox, geographyCheckBox;
    private CheckBox easyCheckBox, hardCheckBox;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_selection);

        biologyCheckBox = findViewById(R.id.biologyCheckBox);
        historyCheckBox = findViewById(R.id.historyCheckBox);
        geographyCheckBox = findViewById(R.id.geographyCheckBox);
        easyCheckBox = findViewById(R.id.easyCheckBox);
        hardCheckBox = findViewById(R.id.hardCheckBox);
        startGameButton = findViewById(R.id.startGameButton);

        startGameButton.setOnClickListener(v -> {
            List<Subject> selectedSubjects = new ArrayList<>();
            List<Difficulty> selectedDifficulties = new ArrayList<>();

            if (biologyCheckBox.isChecked()) selectedSubjects.add(Subject.BIOLOGY);
            if (historyCheckBox.isChecked()) selectedSubjects.add(Subject.HISTORY);
            if (geographyCheckBox.isChecked()) selectedSubjects.add(Subject.GEOGRAPHY);

            if (selectedSubjects.isEmpty()) {
                selectedSubjects.addAll(Arrays.asList(Subject.values()));
            }
            if (easyCheckBox.isChecked()) selectedDifficulties.add(Difficulty.EASY);
            if (hardCheckBox.isChecked()) selectedDifficulties.add(Difficulty.HARD);

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