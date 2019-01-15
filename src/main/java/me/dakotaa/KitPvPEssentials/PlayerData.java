package me.dakotaa.KitPvPEssentials;

import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    private java.util.UUID UUID;
    private String username, killMessage;
    private int kills, deaths, assists, currentStreak, highestStreak;
    private HashMap<String, Float> damageReceived;
    Boolean onStreak;
    public PlayerData(UUID UUID, String username, String killMessage, int kills, int assists, int deaths, int currentStreak, int highestStreak) {
        this.UUID = UUID;
        this.username = username;
        this.killMessage = killMessage;
        this.kills = kills;
        this.assists = assists;
        this.deaths = deaths;
        this.currentStreak = currentStreak;
        this.highestStreak = highestStreak;
        onStreak = false;
        damageReceived = new HashMap<String, Float>();
    }

    public void addDamage(String username, Float dmg) {
        if (damageReceived.containsKey(username)) {
            damageReceived.put(username, damageReceived.get(username) + dmg);
        } else {
            damageReceived.put(username, dmg);
        }
    }

    public HashMap<String, Float> getDamageReceived() {
        return damageReceived;
    }

    public Float getDamageByPlayer(String username) {
        return damageReceived.get(username);
    }

    public void resetDamage() {
        damageReceived.clear();
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

    public Boolean getOnStreak() {
        return onStreak;
    }

    public void setOnStreak(Boolean onStreak) {
        this.onStreak = onStreak;
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

    public int getAssists() {
        return assists;
    }

    public void setAssists(int a) {
        assists = a;
    }

    public void increaseAssists() {
        assists++;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
}
