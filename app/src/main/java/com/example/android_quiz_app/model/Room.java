package com.example.android_quiz_app.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Room implements Serializable {
    private String creatorNickname;
    private List<Subject> subjects;
    private List<Difficulty> difficulties;
    private List<MultiplayerUser> users;
    private boolean isGameStarted;
    private List<Question> questions;

    public Room() {
        // Празен конструктор за Firebase
    }

    public Room(String creatorNickname, List<Subject> subjects, List<Difficulty> difficulties) {
        this.creatorNickname = creatorNickname;
        this.subjects = subjects != null ? subjects : new ArrayList<>();
        this.difficulties = difficulties != null ? difficulties : new ArrayList<>();
        this.users = new ArrayList<>();
        this.isGameStarted = false;
        this.questions = new ArrayList<>();
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Difficulty> getDifficulties() {
        return difficulties;
    }

    public void setDifficulties(List<Difficulty> difficulties) {
        this.difficulties = difficulties;
    }

    public List<MultiplayerUser> getUsers() {
        return users;
    }

    public void setUsers(List<MultiplayerUser> users) {
        this.users = users;
    }

    public void addUser(MultiplayerUser user) {
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(user);
    }

    public void removeUser(MultiplayerUser user) {
        if (users != null) {
            users.remove(user);
        }
    }
    @PropertyName("isGameStarted")
    public boolean isGameStarted() {
        return isGameStarted;
    }
    @PropertyName("isGameStarted")
    public void setIsGameStarted(boolean isGameStarted) {
        this.isGameStarted = isGameStarted;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("creatorNickname", creatorNickname);
        result.put("subjects", subjects);
        result.put("difficulties", difficulties);
        result.put("users", users);
        result.put("isGameStarted", isGameStarted);
        result.put("questions", questions);
        return result;
    }
}