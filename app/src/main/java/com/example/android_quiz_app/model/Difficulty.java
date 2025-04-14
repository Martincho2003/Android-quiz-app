package com.example.android_quiz_app.model;

public enum Difficulty {
    EASY("easy"),
    HARD("hard");

    private final String value;

    Difficulty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}