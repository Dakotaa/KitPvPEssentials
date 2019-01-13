package me.dakotaa.KitPvPEssentials;

import java.util.UUID;

public class PlayerData {
    private java.util.UUID UUID;
    private String username, killMessage;
    private int kills, deaths, currentStreak, highestStreak;
    public PlayerData(UUID UUID, String username, String killMessage, int kills, int deaths, int currentStreak, int highestStreak) {
        this.UUID = UUID;
        this.username = username;
        this.killMessage = killMessage;
        this.kills = kills;
        this.deaths = deaths;
        this.currentStreak = currentStreak;
        this.highestStreak = highestStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public String getKillMessage() {
        return killMessage;
    }

    public void setKillMessage(String killMessage) {
        this.killMessage = killMessage;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public void increaseStreak() {
        currentStreak++;
    }


    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }

    public java.util.UUID getUUID() {

        return UUID;
    }

    public String getUsername() {
        return username;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void increaseDeaths() {
        deaths++;
    }

    public void increaseKills() {
        kills++;
    }

    public int getKills() {

        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
}
