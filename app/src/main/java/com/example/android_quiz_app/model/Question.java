package com.example.android_quiz_app.model;

import java.util.List;

public class Question {
    private String question;
    private List<Answer> answers;

    // Празен конструктор за Firebase
    public Question() {}

    public Question(String question, List<Answer> answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
}