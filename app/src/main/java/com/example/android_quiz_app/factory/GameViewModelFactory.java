package com.example.android_quiz_app.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.viewModel.GameViewModel;

import java.util.List;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private final List<Subject> subjects;
    private final List<Difficulty> difficulties;

    public GameViewModelFactory(List<Subject> subjects, List<Difficulty> difficulties) {
        this.subjects = subjects;
        this.difficulties = difficulties;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GameViewModel.class)) {
            return (T) new GameViewModel(subjects, difficulties);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}