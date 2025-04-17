package com.example.android_quiz_app.model;

public class User {
    private String username;
    private int points;
    private String lastDayPlayed;
    private int playedGamesToday;

    public User() {
    }

    public User(String username, int points, String lastDayPlayed, int playedGamesToday) {
        this.username = username;
        this.points = points;
        this.lastDayPlayed = lastDayPlayed;
        this.playedGamesToday = playedGamesToday;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getLastDayPlayed() {
        return lastDayPlayed;
    }

    public void setLastDayPlayed(String lastDayPlayed) {
        this.lastDayPlayed = lastDayPlayed;
    }

    public int getPlayedGamesToday() {
        return playedGamesToday;
    }

    public void setPlayedGamesToday(int playedGamesToday) {
        this.playedGamesToday = playedGamesToday;
    }
}