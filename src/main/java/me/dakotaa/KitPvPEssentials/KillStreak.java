package me.dakotaa.KitPvPEssentials;

import java.util.ArrayList;

public class KillStreak {
    private int killCount;
    private String message;
    private ArrayList<String> commands;

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<String> commands) {
        this.commands = commands;
    }

    public KillStreak(int killCount, String message, ArrayList<String> commands) {
        this.killCount = killCount;

        this.commands = commands;
        this.message = message;
    }
}
