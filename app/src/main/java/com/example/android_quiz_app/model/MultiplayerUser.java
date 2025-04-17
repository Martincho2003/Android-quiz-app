package com.example.android_quiz_app.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class MultiplayerUser implements Serializable {
    private String username;
    private int gamePoints;

    public MultiplayerUser() {
    }

    public MultiplayerUser(String username) {
        this.username = username;
        this.gamePoints = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public int getGamePoints() {
        return gamePoints;
    }

    public void setGamePoints(int gamePoints) {
        this.gamePoints = gamePoints;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("gamePoints", gamePoints);
        return result;
    }
}