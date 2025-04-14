package com.example.android_quiz_app.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseDatabase database;

    private FirebaseManager() {
        database = FirebaseDatabase.getInstance("https://android-quiz-app-8e645-default-rtdb.europe-west1.firebasedatabase.app");
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public DatabaseReference getUsersReference() {
        return database.getReference("users");
    }

    public DatabaseReference getQuestionsReference() {
        return database.getReference("questions");
    }

    public DatabaseReference getRoomsReference() {
        return database.getReference("rooms");
    }
}