package com.example.android_quiz_app.repository;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_quiz_app.model.Answer;
import com.example.android_quiz_app.model.Difficulty;
import com.example.android_quiz_app.model.Question;
import com.example.android_quiz_app.model.Subject;
import com.example.android_quiz_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameService {
    private static final String TAG = "GameService";
    private final DatabaseReference databaseQuestionReference;
    private final DatabaseReference databaseUserReference;
    private final FirebaseAuth auth;
    private static final int MAX_FETCH_ATTEMPTS = 20; // Максимален брой опити за зареждане на въпроси

    public GameService() {
        this.databaseQuestionReference = FirebaseManager.getInstance().getQuestionsReference();
        this.databaseUserReference = FirebaseManager.getInstance().getUsersReference();
        this.auth = FirebaseAuth.getInstance();
    }

    private LiveData<Question> getFullyRandomQuestion(List<Subject> subjects, List<Difficulty> difficulties) {
        MutableLiveData<Question> questionLiveData = new MutableLiveData<>();

        // Проверка за празни списъци
        if (subjects == null || subjects.isEmpty()) {
            Log.e(TAG, "Subjects list is null or empty");
            questionLiveData.setValue(null);
            return questionLiveData;
        }
        if (difficulties == null || difficulties.isEmpty()) {
            Log.e(TAG, "Difficulties list is null or empty");
            questionLiveData.setValue(null);
            return questionLiveData;
        }

        // Избиране на случаен предмет и трудност
        Random random = new Random();
        Subject subject = subjects.get(random.nextInt(subjects.size()));
        Difficulty difficulty = difficulties.get(random.nextInt(difficulties.size()));

        String path = subject.getValue() + "/" + difficulty.getValue();
        Log.d(TAG, "Fetching question from path: " + path);

        databaseQuestionReference.child(path).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot == null || !snapshot.exists()) {
                    Log.e(TAG, "No questions found at path: " + path);
                    questionLiveData.setValue(null);
                    return;
                }

                Log.d(TAG, "Number of questions at path " + path + ": " + snapshot.getChildrenCount());

                List<Question> questions = new ArrayList<>();

                // Обхождане на всички въпроси
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    Question question = new Question();
                    String questionText = questionSnapshot.child("question").getValue(String.class);
                    if (questionText == null) {
                        Log.w(TAG, "Question text is null for snapshot: " + questionSnapshot.getKey());
                        continue;
                    }
                    question.setQuestion(questionText);
                    question.setDifficulty(difficulty);

                    List<Answer> answers = new ArrayList<>();
                    DataSnapshot answersSnapshot = questionSnapshot.child("answers");
                    if (answersSnapshot.exists()) {
                        Log.d(TAG, "Number of answers for question '" + questionText + "': " + answersSnapshot.getChildrenCount());
                        for (DataSnapshot answerSnapshot : answersSnapshot.getChildren()) {
                            String answerText = answerSnapshot.child("answer").getValue(String.class);
                            String isCorrect = answerSnapshot.child("is_correct").getValue(String.class);
                            if (answerText == null || isCorrect == null) {
                                Log.w(TAG, "Invalid answer in question: " + questionText + ", answer: " + answerSnapshot.getKey());
                                continue;
                            }
                            answers.add(new Answer(answerText, isCorrect));
                        }
                    } else {
                        Log.w(TAG, "No answers found for question: " + questionText);
                    }

                    question.setAnswers(answers);
                    if (!answers.isEmpty()) {
                        questions.add(question);
                        Log.d(TAG, "Added question: " + questionText + " with " + answers.size() + " answers");
                    } else {
                        Log.w(TAG, "Question skipped due to no valid answers: " + questionText);
                    }
                }

                if (!questions.isEmpty()) {
                    // Избиране на случаен въпрос
                    Question randomQuestion = questions.get(random.nextInt(questions.size()));
                    Log.d(TAG, "Selected random question: " + randomQuestion.getQuestion());
                    questionLiveData.setValue(randomQuestion);
                } else {
                    Log.e(TAG, "No valid questions found at path: " + path);
                    questionLiveData.setValue(null);
                }
            } else {
                Log.e(TAG, "Failed to fetch question: " + task.getException().getMessage());
                questionLiveData.setValue(null);
            }
        });

        return questionLiveData;
    }

    public LiveData<List<Question>> getQuestionsFromPub(List<Difficulty> difficulties, List<Subject> subjects) {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();
        List<Question> questions = new ArrayList<>();

        // Рекурсивна функция за извличане на въпроси
        fetchQuestions(questions, subjects, difficulties, 0, questionsLiveData);

        return questionsLiveData;
    }

    private void fetchQuestions(List<Question> questions, List<Subject> subjects, List<Difficulty> difficulties,
                                int currentCount, MutableLiveData<List<Question>> questionsLiveData) {
        // Проверка за максимален брой опити
        if (currentCount >= MAX_FETCH_ATTEMPTS) {
            Log.e(TAG, "Reached max fetch attempts (" + MAX_FETCH_ATTEMPTS + "), stopping. Questions fetched: " + questions.size());
            if (questions.isEmpty()) {
                questionsLiveData.setValue(null); // Ако не сме намерили нито един въпрос, връщаме null
            } else {
                questionsLiveData.setValue(new ArrayList<>(questions)); // Връщаме каквото сме намерили
            }
            return;
        }

        if (questions.size() >= 10) {
            questionsLiveData.setValue(new ArrayList<>(questions.subList(0, 10)));
            Log.d(TAG, "Fetched 10 questions: " + questions.size());
            return;
        }

        getFullyRandomQuestion(subjects, difficulties).observeForever(question -> {
            if (question != null && !containsQuestion(questions, question)) {
                questions.add(question);
                Log.d(TAG, "Added question: " + question.getQuestion() + ", Total questions: " + questions.size());
            } else {
                Log.w(TAG, "Question is null or already exists, attempt: " + (currentCount + 1));
            }

            fetchQuestions(questions, subjects, difficulties, currentCount + 1, questionsLiveData);
        });
    }

    private boolean containsQuestion(List<Question> questions, Question question) {
        for (Question q : questions) {
            if (q.getQuestion().equals(question.getQuestion())) {
                return true;
            }
        }
        return false;
    }

    public LiveData<User> getUserDetails() {
        MutableLiveData<User> userDetailsLiveData = new MutableLiveData<>();

        String userId = auth.getCurrentUser().getUid();
        databaseUserReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                User userDetails = snapshot.getValue(User.class);
                userDetailsLiveData.setValue(userDetails);
                Log.d(TAG, "User details fetched: " + (userDetails != null ? userDetails.getUsername() : "null"));
            } else {
                Log.e(TAG, "Failed to fetch user details: " + task.getException().getMessage());
                userDetailsLiveData.setValue(null);
            }
        });

        return userDetailsLiveData;
    }

    public void sendPoints(int gamePoints) {
        String userId = auth.getCurrentUser().getUid();
        getUserDetails().observeForever(userDetails -> {
            if (userDetails != null) {
                DatabaseReference userRef = databaseUserReference.child(userId);
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String currentDate = format.format(new Date());

                if (gamePoints < 0) {
                    userRef.child("points").setValue(userDetails.getPoints() + gamePoints);
                    userRef.child("playedGamesToday").setValue(userDetails.getPlayedGamesToday() + 1);
                    Log.d(TAG, "Updated points (negative): " + (userDetails.getPoints() + gamePoints));
                } else {
                    try {
                        Date lastPlayedDate = format.parse(userDetails.getLastDayPlayed());
                        boolean isSameDay = lastPlayedDate != null && Calendar.getInstance().getTime().getTime() - lastPlayedDate.getTime() < 24 * 60 * 60 * 1000;

                        if (isSameDay) {
                            if (userDetails.getPlayedGamesToday() < 3) {
                                userRef.child("points").setValue(userDetails.getPoints() + gamePoints);
                                userRef.child("playedGamesToday").setValue(userDetails.getPlayedGamesToday() + 1);
                                Log.d(TAG, "Updated points (same day): " + (userDetails.getPoints() + gamePoints));
                            } else {
                                Log.w(TAG, "User has reached the daily game limit");
                            }
                        } else {
                            userRef.child("points").setValue(userDetails.getPoints() + gamePoints);
                            userRef.child("lastDayPlayed").setValue(currentDate);
                            userRef.child("playedGamesToday").setValue(1);
                            Log.d(TAG, "Updated points (new day): " + (userDetails.getPoints() + gamePoints));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date: " + e.getMessage());
                    }
                }
            } else {
                Log.e(TAG, "User details are null when sending points");
            }
        });
    }
}