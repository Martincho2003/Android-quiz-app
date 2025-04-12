package com.example.android_quiz_app.factory;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.android_quiz_app.repository.FirebaseRepository;
import com.example.android_quiz_app.viewModel.MainViewModel;

public class MainViewModelFactory implements ViewModelProvider.Factory {
    private final FirebaseRepository repository;

    public MainViewModelFactory(FirebaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}