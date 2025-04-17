package com.example.android_quiz_app.model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private String question;
    private List<Answer> answers;
    private Difficulty difficulty;

    public Question() {}

    public Question(String question, List<Answer> answers, Difficulty difficulty) {
        this.question = question;
        this.answers = answers;
        this.difficulty = difficulty != null ? difficulty : Difficulty.EASY;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}