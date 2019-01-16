package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class SplitKill {

    private LinkedHashMap<String, KillMessage> killMessages;
    private HashMap<Integer, KillStreak> killStreaks;

    // TODO: Fix killstreaks
    // TODO: Kill message based on kill, assist, or finish.

    // Main function to process kills.
    // Given a victim, last hitter, and a reference to the player database, calculates the percent damage, sends messages, runs commands, modifies stats.
    public static void ProcessKill(Player victim, Player killer, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {

        PlayerData victimData = database.get(victim.getUniqueId());
        HashMap<String, Float> damageReceived = victimData.getDamageReceived();
        HashMap<String, Float> damagePercent = calculateDamage(damageReceived);



        String highestDMG =  getHighestDamager(damagePercent);
        String secondDMG = getSecondDamager(damagePercent);

        for (String name : damagePercent.keySet()) {
            if (damagePercent.get(name) > 0.2) {
                if (Bukkit.getPlayer(name) != null) {
                    Bukkit.getPlayer(name).sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou did &c&l" + damagePercent.get(name) * 100 + "% &7&ldamage to &a&l" + victim.getName()));
                }
            }
        }

        addKill(highestDMG, victim.getName(), database, killStreaks);

        if (!secondDMG.equals("")) {
            if (damagePercent.get(secondDMG) > 0.25) {
                addAssist(secondDMG, victim.getName(), database, killStreaks);
            }
        }

        addDeath(victim.getName(), database, killStreaks);

        Bukkit.getLogger().info("Most damage done by: " + getHighestDamager(damagePercent));
        Bukkit.getLogger().info("Second most damage done by: " + getSecondDamager(damagePercent));

        if (victimData.getOnStreak()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l%killer% &c&ljust ended &4&l%victim%'s &c&l%streak% player killstreak!").replace("%killer%", killer.getName()).replace("%victim%", victim.getName()).replace("%streak%", valueOf(victimData.getCurrentStreak())));
        }

        try {
            payDamagers(damagePercent);
        } catch (Exception e) {
            Bukkit.getLogger().info("Error paying damagers: " + e.toString());
        }
    }

    private static void addKill(String k, String victim, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        if (Bukkit.getPlayer(k) != null) {
            Player killer = Bukkit.getPlayer(k);
            PlayerData killerData = database.get(killer.getUniqueId());

            killerData.increaseKills();
            killerData.increaseStreak();

            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou did the most damage to &a&l" + victim + " &7&land got a kill"));

            if (killerData.getCurrentStreak() > killerData.getHighestStreak()) {
                killerData.setHighestStreak(killerData.getCurrentStreak());
            }

            if (streakHit(killerData.getCurrentStreak(), killStreaks)) {
                for (String command : getKillStreak(killerData.getCurrentStreak(), killStreaks).getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", killer.getName()).replace("%killed%", victim).replace("%streak%", valueOf(killerData.getCurrentStreak())));
                }
            }
        }
    }

    private static void addAssist(String k, String victim, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        if (Bukkit.getPlayer(k) != null) {
            Player assister = Bukkit.getPlayer(k);
            PlayerData assisterData = database.get(assister.getUniqueId());

            assisterData.increaseAssists();

            assister.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou did the second most damage to &a&l" + victim + " &7&land got an assist."));
        }
    }

    private static void addDeath(String v, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        if (Bukkit.getPlayer(v) != null) {
            Player victim = Bukkit.getPlayer(v);
            PlayerData victimData = database.get(victim.getUniqueId());

            if (victimData.getCurrentStreak() > victimData.getHighestStreak()) {
                victimData.setHighestStreak(victimData.getCurrentStreak());
            }

            victimData.setCurrentStreak(0);
            victimData.setOnStreak(false);
            victimData.increaseDeaths();
        }
    }

    private static Boolean streakHit(int streak, HashMap<Integer, KillStreak> killStreaks) {
        return (killStreaks.keySet().contains(streak));
    }

    private static KillStreak getKillStreak(int streak, HashMap<Integer, KillStreak> killStreaks) {
        if (killStreaks.keySet().contains(streak)) {
            return killStreaks.get(streak);
        } else {
            return null;
        }
    }

    public static HashMap<String, Float> calculateDamage(HashMap<String, Float> playerDamage) {
        Float totalDamage = 0f;
        HashMap<String, Float> damagePercents = new HashMap<String, Float>();
        for (Float dmg : playerDamage.values()) {
            totalDamage += dmg;
        }
        for (String player : playerDamage.keySet()) {
            damagePercents.put(player, playerDamage.get(player)/totalDamage);
        }
        return damagePercents;
    }

    public static String getHighestDamager(HashMap<String, Float> damagePercents) {
        Float highest = 0f;
        String player = "";
        for (String username : damagePercents.keySet()) {
            if (damagePercents.get(username) > highest) {
                highest = damagePercents.get(username);
                player = username;
            }
        }

        return player;
    }

    public static String getSecondDamager(HashMap<String, Float> damagePercents) {
        Float highest = 0f;
        String player = "";
        String highestPlayer = getHighestDamager(damagePercents);
        for (String username : damagePercents.keySet()) {
            if (damagePercents.get(username) > highest && !username.equals(highestPlayer)) {
                highest = damagePercents.get(username);
                player = username;
            }
        }

        return player;
    }

    private static void payDamagers(HashMap<String, Float> damagePercent) {
        if (damagePercent.keySet().size() > 1 && (damagePercent.get(getSecondDamager(damagePercent)) > 0.25)) {
            Float payout = 5.0f;

            String player1 = getHighestDamager(damagePercent);
            String player2 = getSecondDamager(damagePercent);
            Float player1dmg = damagePercent.get(player1);
            Float player2dmg = damagePercent.get(player2);

            Bukkit.getLogger().info("in payDamagers");
            Bukkit.getLogger().info(valueOf(payout));
            Float totalPercent = player1dmg + player2dmg;
            Bukkit.getLogger().info(valueOf(totalPercent));
            Float payout1 = payout * (player1dmg / totalPercent);
            Bukkit.getLogger().info(valueOf(payout1));
            Float payout2 = payout * (player2dmg / totalPercent);
            Bukkit.getLogger().info(valueOf(payout2));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player1 + " " + payout1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player2 + " " + payout2);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + getHighestDamager(damagePercent) + " 5.0");
        }
    }

    public static ArrayList<String> getHighestDamagers(HashMap<String, Float> damagePercents) {
        Float highest = 0f;
        Float secondhighest = 0f;
        String first = "";
        String second = "";
        ArrayList<String> pair = new ArrayList<String>();

        for (String username : damagePercents.keySet()) {
            if (damagePercents.get(username) > secondhighest) {
                secondhighest = highest;
                second = first;
                highest = damagePercents.get(username);
                first = username;
            }
        }

        pair.add(first);
        pair.add(second);

        return pair;
    }
}
