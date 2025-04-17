package com.example.android_quiz_app.model;

public enum Subject {
    BIOLOGY("biology"),
    HISTORY("history"),
    GEOGRAPHY("geography")
    ;

    private final String value;

    Subject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}