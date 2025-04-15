package com.example.android_quiz_app.viewModel;

import android.os.CountDownTimer;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.android_quiz_app.model.MultiplayerUser;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Room;
import com.example.android_quiz_app.repository.MultiplayerService;
import java.util.List;

public class MultiplayerGameViewModel extends ViewModel {
    private static final String TAG = "MultiplayerGameViewModel";
    private static final long QUESTION_TIME_MS = 30000; // 30 секунди на въпрос
    private final MultiplayerService multiplayerService;
    private final MutableLiveData<Room> room;
    private final MutableLiveData<MultiplayerUser> currentUser;
    private final MutableLiveData<Question> currentQuestion;
    private final MutableLiveData<Integer> currentQuestionIndex;
    private final MutableLiveData<Long> timeLeft;
    private final MutableLiveData<Boolean> gameFinished;
    private CountDownTimer timer;
    private List<Question> questions;

    public MultiplayerGameViewModel(Room initialRoom) {
        multiplayerService = MultiplayerService.getInstance();
        room = new MutableLiveData<>(initialRoom);
        currentUser = new MutableLiveData<>();
        currentQuestion = new MutableLiveData<>();
        currentQuestionIndex = new MutableLiveData<>(0);
        timeLeft = new MutableLiveData<>(QUESTION_TIME_MS);
        gameFinished = new MutableLiveData<>(false);

        questions = initialRoom.getQuestions();
        if (!questions.isEmpty()) {
            currentQuestion.setValue(questions.get(0));
        }

        // Зареждаме текущия потребител
        for (MultiplayerUser user : initialRoom.getUsers()) {
            if (user.getUsername().equals(getCurrentUsername())) {
                currentUser.setValue(user);
                break;
            }
        }

        // Наблюдаваме промените в стаята
        multiplayerService.getRooms().observeForever(rooms -> {
            for (Room updatedRoom : rooms) {
                if (updatedRoom.getCreatorNickname().equals(initialRoom.getCreatorNickname())) {
                    room.setValue(updatedRoom);
                    if (currentQuestionIndex.getValue() != null && currentQuestionIndex.getValue() >= questions.size()) {
                        gameFinished.setValue(true);
                    }
                    break;
                }
            }
        });

        startTimer();
    }

    private String getCurrentUsername() {
        // Това трябва да бъде заменено с реална логика за текущия потребител
        // Например чрез GameService или SharedPreferences
        return currentUser.getValue() != null ? currentUser.getValue().getUsername() : "";
    }

    private void startTimer() {
        timer = new CountDownTimer(QUESTION_TIME_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                submitAnswer(-1); // -1 индикира, че времето е изтекло
            }
        }.start();
    }

    public void submitAnswer(int selectedAnswerIndex) {
        if (timer != null) {
            timer.cancel();
        }

        Question question = currentQuestion.getValue();
        MultiplayerUser user = currentUser.getValue();
        Room currentRoom = room.getValue();
        if (question == null || user == null || currentRoom == null) return;

        // Проверяваме дали отговорът е правилен
        if (selectedAnswerIndex != -1 && question.getAnswers().get(selectedAnswerIndex).getIs_correct().equals("1")) {
            user.setGamePoints(user.getGamePoints() + 4); // Добавяме точки за правилен отговор
        }

        // Актуализираме точките в стаята
        multiplayerService.updateUserPoints(currentRoom, user);
        currentUser.setValue(user);

        // Преминаваме към следващия въпрос
        int nextIndex = currentQuestionIndex.getValue() + 1;
        if (nextIndex < questions.size()) {
            currentQuestionIndex.setValue(nextIndex);
            currentQuestion.setValue(questions.get(nextIndex));
            timeLeft.setValue(QUESTION_TIME_MS);
            startTimer();
        } else {
            gameFinished.setValue(true);
        }
    }

    public LiveData<Room> getRoom() {
        return room;
    }

    public LiveData<MultiplayerUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }

    public LiveData<Integer> getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public LiveData<Long> getTimeLeft() {
        return timeLeft;
    }

    public LiveData<Boolean> getGameFinished() {
        return gameFinished;
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    @Override
    protected void onCleared() {
        if (timer != null) {
            timer.cancel();
        }
        super.onCleared();
    }
}