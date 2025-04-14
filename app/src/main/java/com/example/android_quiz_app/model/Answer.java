package com.example.android_quiz_app.model;
public class Answer {
    private String answer;
    private String is_correct;

    public Answer() {}

    public Answer(String answer, String is_correct) {
        this.answer = answer;
        this.is_correct = is_correct;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(String is_correct) {
        this.is_correct = is_correct;
    }
}