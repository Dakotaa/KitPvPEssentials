package me.dakotaa.KitPvPEssentials;

import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    private java.util.UUID UUID;
    private String username, killMessage;
    private int kills, deaths, assists, currentStreak, highestStreak;
    private HashMap<String, Float> damageReceived;
    private Boolean onStreak;

    PlayerData(UUID UUID, String username, String killMessage, int kills, int assists, int deaths, int currentStreak, int highestStreak) {
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


    void addDamage(String username, Float dmg) {
        if (damageReceived.containsKey(username)) {
            damageReceived.put(username, damageReceived.get(username) + dmg);
        } else {
            damageReceived.put(username, dmg);
        }
    }


    HashMap<String, Float> getDamageReceived() {
        return damageReceived;
    }


    // Function to decay damage - removes x damage from each player.
    void decayDamage(Float x) {
        for (String name : damageReceived.keySet()) {
            Float dmg = damageReceived.get(name);
            if (dmg <= 0) {
                damageReceived.remove(name);
            } else {
                if (dmg - x < 0) {
                    damageReceived.put(name, 0.0f);
                } else {
                    damageReceived.put(name, dmg - x);
                }
            }
        }
    }


    public Float getDamageByPlayer(String username) {
        return damageReceived.get(username);
    }


    void resetDamage() {
        damageReceived.clear();
    }


    int getCurrentStreak() {
        return currentStreak;
    }


    String getKillMessage() {
        return killMessage;
    }


    void setKillMessage(String killMessage) {
        this.killMessage = killMessage;
    }


    Boolean getOnStreak() {
        return onStreak;
    }


    void setOnStreak(Boolean onStreak) {
        this.onStreak = onStreak;
    }


    void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }


    void increaseStreak() {
        currentStreak++;
    }


    int getHighestStreak() {
        return highestStreak;
    }


    void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }


    java.util.UUID getUUID() {

        return UUID;
    }


    public String getUsername() {
        return username;
    }


    int getDeaths() {
        return deaths;
    }


    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }


    void increaseDeaths() {
        deaths++;
    }


    void increaseKills() {
        kills++;
    }


    int getKills() {

        return kills;
    }


    int getAssists() {
        return assists;
    }


    public void setAssists(int a) {
        assists = a;
    }


    void increaseAssists() {
        assists++;
    }


    public void setKills(int kills) {
        this.kills = kills;
    }
}
