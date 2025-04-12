package com.example.android_quiz_app.repository;

import com.example.android_quiz_app.model.Question;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    private final FirebaseDatabase database;

    public QuizRepository() {
        database = FirebaseDatabase.getInstance("https://android-quiz-app-8e645-default-rtdb.europe-west1.firebasedatabase.app");
    }

    public interface OnQuestionLoadedListener {
        void onQuestionLoaded(Question question);
        void onError(String error);
    }

    public void getFirstQuestion(OnQuestionLoadedListener listener) {
        database.getReference("questions/biology/easy")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                            listener.onError("Няма данни в базата");
                            return;
                        }

                        DataSnapshot firstQuestionSnapshot = dataSnapshot.getChildren().iterator().next();
                        Question question = firstQuestionSnapshot.getValue(Question.class);
                        if (question != null) {
                            listener.onQuestionLoaded(question);
                        } else {
                            listener.onError("Грешка при десериализация на въпроса");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onError("Firebase грешка: " + databaseError.getMessage());
                    }
                });
    }
}