package com.example.android_quiz_app.viewModel;

import android.os.CountDownTimer;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.repository.MultiplayerService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiplayerGameViewModel extends ViewModel {
    private static final String TAG = "MultiplayerGameViewModel";
    private final Room room;
    private final MultiplayerService multiplayerService;
    private final MutableLiveData<List<Question>> currentQuestions = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> points = new MutableLiveData<>(0);
    private final MutableLiveData<Long> currentQuestionTime = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gameFinished = new MutableLiveData<>(false);
    private final MutableLiveData<List<MultiplayerUser>> leaderboard = new MutableLiveData<>();
    private CountDownTimer timer;
    private MultiplayerUser currentUser;

    public MultiplayerGameViewModel(Room room) {
        this.room = room;
        this.multiplayerService = MultiplayerService.getInstance();
        this.currentQuestions.setValue(room.getQuestions());

        if (room.getQuestions() == null || room.getQuestions().isEmpty()) {
            Log.e(TAG, "No questions available for the game");
            gameFinished.setValue(true);
            return;
        }

        multiplayerService.getMultiplayerUserDetails().observeForever(user -> {
            if (user != null) {
                for (MultiplayerUser muser : room.getUsers()) {
                    Log.d(TAG, "Checking user: " + muser.getUsername());
                    if (muser.getUsername().equals(user.getUsername())) {
                        this.currentUser = muser;
                        break;
                    }
                }
            } else {
                currentUser = null;
            }
        });
        if (currentUser == null) {
            Log.e(TAG, "Current user not found in room");
        }

        List<Question> questions = currentQuestions.getValue();
        if (questions != null && !questions.isEmpty() && currentQuestionIndex.getValue() == 0) {
            startTimer();
        }

        updateLeaderboard();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        List<Question> questions = currentQuestions.getValue();
        if (questions == null || currentIndex >= questions.size()) {
            endGame();
            return;
        }

        Question currentQuestion = questions.get(currentIndex);
        long timeInMillis = (currentQuestion.getDifficulty() == Difficulty.HARD) ? 31000 : 21000;
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
        Log.d(TAG, "Timer started for question " + currentIndex);
    }

    public void checkAnswer(Answer answer) {
        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        List<Question> questions = currentQuestions.getValue();
        if (questions == null || currentIndex >= questions.size()) {
            return;
        }

        boolean isCorrect = "1".equalsIgnoreCase(answer.getIs_correct());
        if (isCorrect) {
            int currentPoints = points.getValue() != null ? points.getValue() : 0;
            int pointsToAdd = questions.get(currentIndex).getDifficulty() == Difficulty.HARD ? 6 : 3;
            points.setValue(currentPoints + pointsToAdd);
            currentUser.setGamePoints(currentPoints + pointsToAdd);
            Log.d(TAG, "Correct answer, points updated locally to: " + (currentPoints + pointsToAdd));
        } else {
            Log.d(TAG, "Incorrect answer, no points added");
        }
        nextQuestion();
    }

    public void nextQuestion() {
        if (timer != null) {
            timer.cancel();
        }

        int currentIndex = currentQuestionIndex.getValue() != null ? currentQuestionIndex.getValue() : 0;
        currentIndex++;
        if (currentIndex < currentQuestions.getValue().size()) {
            currentQuestionIndex.setValue(currentIndex);
            startTimer();
        } else {
            endGame();
        }
        Log.d(TAG, "Moving to question " + currentIndex);
    }

    private void endGame() {
        if (timer != null) {
            timer.cancel();
        }
        // Синхронизиране на точките с Firebase само при приключване на играта
        multiplayerService.updateUserPoints(room, currentUser);
        updateLeaderboard();
        gameFinished.setValue(true);
        Log.d(TAG, "Game ended with points: " + points.getValue() + ", synced to Firebase");
    }

    private void updateLeaderboard() {
        List<MultiplayerUser> users = room.getUsers();
        if (users != null) {
            Collections.sort(users, new Comparator<MultiplayerUser>() {
                @Override
                public int compare(MultiplayerUser u1, MultiplayerUser u2) {
                    return Integer.compare(u2.getGamePoints(), u1.getGamePoints()); // Сортиране в намаляващ ред
                }
            });
            leaderboard.setValue(users);
        }
    }

    public LiveData<List<Question>> getCurrentQuestions() {
        return currentQuestions;
    }

    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public LiveData<Integer> getPoints() {
        return points;
    }

    public LiveData<Long> getCurrentQuestionTime() {
        return currentQuestionTime;
    }

    public LiveData<Boolean> getGameFinished() {
        return gameFinished;
    }

    public LiveData<List<MultiplayerUser>> getLeaderboard() {
        return leaderboard;
    }

    public void setCurrentUser(MultiplayerUser user) {
        this.currentUser = user;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) {
            timer.cancel();
        }
    }
}