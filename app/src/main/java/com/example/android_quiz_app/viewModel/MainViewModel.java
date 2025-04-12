package com.example.android_quiz_app.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.repository.FirebaseRepository;

public class MainViewModel extends ViewModel {

    private final FirebaseRepository repository;
    private final MutableLiveData<Question> question = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MainViewModel(FirebaseRepository repository) {
        this.repository = repository;
        loadFirstQuestion();
    }

    public LiveData<Question> getQuestion() {
        return question;
    }

    public LiveData<String> getError() {
        return error;
    }

    private void loadFirstQuestion() {
        repository.getFirstQuestion(new FirebaseRepository.OnQuestionLoadedListener() {
            @Override
            public void onQuestionLoaded(Question loadedQuestion) {
                question.setValue(loadedQuestion);
            }

            @Override
            public void onError(String errorMessage) {
                error.setValue(errorMessage);
            }
        });
    }
}