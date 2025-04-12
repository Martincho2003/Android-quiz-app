package com.example.android_quiz_app.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.repository.QuizRepository;

public class MainViewModel extends ViewModel {

    private final QuizRepository repository;
    private final MutableLiveData<Question> question = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MainViewModel(QuizRepository repository) {
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
        repository.getFirstQuestion(new QuizRepository.OnQuestionLoadedListener() {
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