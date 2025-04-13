package com.example.android_quiz_app.viewModel;

import android.os.CountDownTimer;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.model.User;
import com.example.android_quiz_app.repository.GameService;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameViewModel extends ViewModel {
    private static final String TAG = "GameViewModel";
    private final GameService gameService;
    private final MutableLiveData<List<Question>> questions = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> points = new MutableLiveData<>(0);
    private final MutableLiveData<List<Integer>> seconds = new MutableLiveData<>();
    private final MutableLiveData<List<Boolean>> isAddTime = new MutableLiveData<>();
    private final MutableLiveData<List<Boolean>> isExclude = new MutableLiveData<>();
    private final MutableLiveData<User> userDetails = new MutableLiveData<>();
    private final MutableLiveData<Long> currentQuestionTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> gameEnded = new MutableLiveData<>();
    private CountDownTimer timer;
    private List<Question> currentQuestionsList = new ArrayList<>();
    private List<Integer> currentSecondsList = new ArrayList<>();
    private List<Boolean> currentIsAddTimeList = new ArrayList<>();
    private List<Boolean> currentIsExcludeList = new ArrayList<>();
    private boolean isListsInitialized = false;

    public GameViewModel(List<Subject> subjects, List<Difficulty> difficulties) {
        gameService = new GameService();
        loadQuestions(subjects, difficulties);
        loadUserDetails();
    }

    private void loadQuestions(List<Subject> subjects, List<Difficulty> difficulties) {
        gameService.getQuestionsFromPub(difficulties, subjects).observeForever(loadedQuestions -> {
            if (loadedQuestions != null) {
                currentQuestionsList = loadedQuestions;
                currentSecondsList.clear();
                currentIsAddTimeList.clear();
                currentIsExcludeList.clear();

                for (Question question : loadedQuestions) {
                    currentSecondsList.add(question.getDifficulty() == Difficulty.HARD ? 30 : 20);
                    currentIsAddTimeList.add(false);
                    currentIsExcludeList.add(false);
                }

                isListsInitialized = true;

                questions.setValue(loadedQuestions);
                seconds.setValue(currentSecondsList);
                isAddTime.setValue(currentIsAddTimeList);
                isExclude.setValue(currentIsExcludeList);

                Log.d(TAG, "Lists initialized: questions=" + currentQuestionsList.size() +
                        ", seconds=" + currentSecondsList.size() +
                        ", addTime=" + currentIsAddTimeList.size() +
                        ", exclude=" + currentIsExcludeList.size());

                startTimer();
            } else {
                Log.e(TAG, "Failed to load questions");
                questions.setValue(null);
            }
        });
    }

    private void loadUserDetails() {
        gameService.getUserDetails().observeForever(details -> {
            userDetails.setValue(details);
        });
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        if (currentIndex >= currentQuestionsList.size()) {
            endGame();
            return;
        }

        long timeInMillis = currentSecondsList.get(currentIndex) * 1000L;
        timer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentQuestionTime.setValue(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                nextQuestion();
            }
        }.start();
    }

    public void checkAnswer(Answer answer) {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        if ("1".equalsIgnoreCase(answer.getIs_correct())) {
            int currentPoints = points.getValue() != null ? points.getValue() : 0;
            int pointsToAdd = currentQuestionsList.get(currentIndex).getDifficulty() == Difficulty.HARD ? 6 : 3;
            points.setValue(currentPoints + pointsToAdd);
        }
        nextQuestion();
    }

    public void nextQuestion() {
        if (timer != null) {
            timer.cancel();
        }

        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        currentIndex++;
        if (currentIndex < currentQuestionsList.size()) {
            currentQuestionIndex.setValue(currentIndex);
            startTimer();
        } else {
            endGame();
        }
    }

    private void endGame() {
        int finalPoints = points.getValue() != null ? points.getValue() : 0;
        gameService.sendPoints(finalPoints);
        Log.d(TAG, "Game ended with points: " + finalPoints);
        gameEnded.setValue(finalPoints);
    }

    public void addTime() {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        long remainingTime = currentQuestionTime.getValue() != null ? currentQuestionTime.getValue() : 0;
        currentSecondsList.set(currentIndex, (int) (remainingTime + 20));
        seconds.setValue(currentSecondsList);

        int currentPoints = points.getValue() != null ? points.getValue() : 0;
        int pointsToDeduct = currentQuestionsList.get(currentIndex).getDifficulty() == Difficulty.HARD ? 2 : 1;
        points.setValue(currentPoints - pointsToDeduct);

        currentIsAddTimeList.set(currentIndex, true);
        isAddTime.setValue(currentIsAddTimeList);

        startTimer();
    }

    public void excludeAnswers() {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        List<Answer> answers = currentQuestionsList.get(currentIndex).getAnswers();

        if (currentIsExcludeList.get(currentIndex) || answers.size() <= 2) {
            Log.d(TAG, "Exclude Answers already used or answers already reduced to 2 for question " + currentIndex);
            return;
        }

        while (answers.size() > 2) {
            List<Answer> incorrectAnswers = new ArrayList<>();
            for (Answer answer : answers) {
                if (!"true".equalsIgnoreCase(answer.getIs_correct())) {
                    incorrectAnswers.add(answer);
                }
            }
            if (!incorrectAnswers.isEmpty()) {
                answers.remove(incorrectAnswers.get(new Random().nextInt(incorrectAnswers.size())));
            }
        }
        currentQuestionsList.get(currentIndex).setAnswers(answers);
        questions.setValue(currentQuestionsList);

        int currentPoints = points.getValue() != null ? points.getValue() : 0;
        int pointsToDeduct = currentQuestionsList.get(currentIndex).getDifficulty() == Difficulty.HARD ? 4 : 2;
        points.setValue(currentPoints - pointsToDeduct);

        currentIsExcludeList.set(currentIndex, true);
        isExclude.setValue(currentIsExcludeList);
    }

    public boolean isAddTimeDeactivated() {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        User details = userDetails.getValue();
        int currentPoints = points.getValue() != null ? points.getValue() : 0;

        if (!isListsInitialized || details == null || currentQuestionsList.isEmpty() ||
                currentIndex >= currentQuestionsList.size() || currentIsAddTimeList.isEmpty() ||
                currentIndex >= currentIsAddTimeList.size()) {
            Log.w(TAG, "AddTime deactivated: Lists not initialized or invalid state. " +
                    "isListsInitialized=" + isListsInitialized +
                    ", userDetails=" + (details != null) +
                    ", questionsSize=" + currentQuestionsList.size() +
                    ", addTimeSize=" + currentIsAddTimeList.size() +
                    ", currentIndex=" + currentIndex);
            return true;
        }

        int totalPoints = details.getPoints() + currentPoints;
        if (currentQuestionsList.get(currentIndex).getDifficulty() == Difficulty.HARD) {
            return totalPoints < 2 || currentIsAddTimeList.get(currentIndex);
        } else {
            return totalPoints < 1 || currentIsAddTimeList.get(currentIndex);
        }
    }

    public boolean isExcludeDeactivated() {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        User details = userDetails.getValue();
        int currentPoints = points.getValue() != null ? points.getValue() : 0;

        if (!isListsInitialized || details == null || currentQuestionsList.isEmpty() ||
                currentIndex >= currentQuestionsList.size() || currentIsExcludeList.isEmpty() ||
                currentIndex >= currentIsExcludeList.size()) {
            Log.w(TAG, "Exclude deactivated: Lists not initialized or invalid state. " +
                    "isListsInitialized=" + isListsInitialized +
                    ", userDetails=" + (details != null) +
                    ", questionsSize=" + currentQuestionsList.size() +
                    ", excludeSize=" + currentIsExcludeList.size() +
                    ", currentIndex=" + currentIndex);
            return true;
        }

        int totalPoints = details.getPoints() + currentPoints;
        if (currentQuestionsList.get(currentIndex).getDifficulty() == Difficulty.HARD) {
            return totalPoints < 4 || currentIsExcludeList.get(currentIndex);
        } else {
            return totalPoints < 2 || currentIsExcludeList.get(currentIndex);
        }
    }

    public LiveData<List<Question>> getQuestions() {
        return questions;
    }

    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public LiveData<Integer> getPoints() {
        return points;
    }

    public LiveData<List<Integer>> getSeconds() {
        return seconds;
    }

    public LiveData<Long> getCurrentQuestionTime() {
        return currentQuestionTime;
    }

    public LiveData<Integer> getGameEnded() {
        return gameEnded;
    }

    public LiveData<User> getUserDetails() {
        return userDetails;
    }

    public LiveData<List<Boolean>> getIsAddTime() {
        return isAddTime;
    }

    public LiveData<List<Boolean>> getIsExclude() {
        return isExclude;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}