package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class SplitKill {

    private static DecimalFormat round = new DecimalFormat("##.00");

    // Main function to process kills.
    // Given a victim, last hitter, and a reference to the player database, calculates the percent damage, sends messages, runs commands, modifies stats.
    static void ProcessKill(Player victim, Player killer, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        PlayerData victimData = database.get(victim.getUniqueId());
        HashMap<String, Float> damageReceived = victimData.getDamageReceived();
        HashMap<String, Float> damagePercent = calculateDamage(damageReceived);

        String highestDMG =  getHighestDamager(damagePercent);
        String secondDMG = getSecondDamager(damagePercent);

        // Notify a player how much damage they did if they are in the correct range of someone they did at least 20% damage to.
        for (String name : damagePercent.keySet()) {
            if (damagePercent.get(name) > 0.2) {
                if (Bukkit.getPlayer(name) != null) {
                    Location loc1 = Bukkit.getPlayer(name).getLocation();
                    Location loc2 = victim.getLocation();
                    if (loc1.distance(loc2) < 30) {
                        Bukkit.getPlayer(name).sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou did &c&l" + round.format(damagePercent.get(name) * 100) + "% &7&ldamage to &a&l" + victim.getName() + "&7&l."));
                    }
                }
            }
        }

        // Adds a kill to the stats of the player who did the most damage.
        addKill(highestDMG, victim.getName(), database, killStreaks);

        // If there is a player that has done the most damage and more than 20%, gives them an assist.
        if (!secondDMG.equals("")) {
            if (damagePercent.get(secondDMG) > 0.2) {
                addAssist(secondDMG, victim.getName(), database, killStreaks);
            }
        }

        // Add a death to the stats of the victim.
        addDeath(victim.getName(), database, killStreaks);

        // If the victim was on a streak, announces that their streak has been ended by the killer.
        if (victimData.getOnStreak()) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l%killer% &c&ljust ended &4&l%victim%'s &c&l%streak% player killstreak!").replace("%killer%", killer.getName()).replace("%victim%", victim.getName()).replace("%streak%", valueOf(victimData.getCurrentStreak())));
        }

        // Splits the pay between the top 1-2 damagers based on percent damage done.
        try {
            payDamagers(damagePercent);
        } catch (Exception e) {
            Bukkit.getLogger().info("Error paying damagers: " + e.toString());
        }
    }


    // Adds a kill to the given player's stats, increasing their streak and running streak commands if a streak is hit.
    private static void addKill(String k, String victim, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        if (Bukkit.getPlayer(k) != null) {
            Player killer = Bukkit.getPlayer(k);
            PlayerData killerData = database.get(killer.getUniqueId());

            killerData.increaseKills();
            killerData.increaseStreak();

            killer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou got the &c&lkill &7&lfor doing the most damage to &a&l" + victim + "&7&l."));

            if (killerData.getCurrentStreak() > killerData.getHighestStreak()) {
                killerData.setHighestStreak(killerData.getCurrentStreak());
            }

            // If the player hit a streak on this kill, broadcast the streak message in chat.
            if (streakHit(killerData.getCurrentStreak(), killStreaks)) {
                Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', getKillStreak(killerData.getCurrentStreak(), killStreaks).getMessage().replace("%player%", killer.getName())));
            }

            if (streakHit(killerData.getCurrentStreak(), killStreaks)) {
                killerData.setOnStreak(true);
                for (String command : getKillStreak(killerData.getCurrentStreak(), killStreaks).getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", killer.getName()).replace("%killed%", victim).replace("%streak%", valueOf(killerData.getCurrentStreak())));
                }
            }
        }
    }


    // Adds an assist to the given player's stats.
    private static void addAssist(String k, String victim, HashMap<UUID, PlayerData> database, HashMap<Integer, KillStreak> killStreaks) {
        if (Bukkit.getPlayer(k) != null) {
            Player assister = Bukkit.getPlayer(k);
            PlayerData assisterData = database.get(assister.getUniqueId());

            assisterData.increaseAssists();
            assister.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lYou got the &e&lassist &7&lfor doing the second most damage to &a&l" + victim + "&7&l."));
        }
    }


    // Adds a death to the given player's stats, ending any current streak they have.
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


    // Checks if the given streak has a defined streak reward.
    private static Boolean streakHit(int streak, HashMap<Integer, KillStreak> killStreaks) {
        return (killStreaks.keySet().contains(streak));
    }


    // Returns the streak information of a killstreak
    private static KillStreak getKillStreak(int streak, HashMap<Integer, KillStreak> killStreaks) {
        if (killStreaks.keySet().contains(streak)) {
            return killStreaks.get(streak);
        } else {
            return null;
        }
    }


    // Calculates how much damage each player did to the given victim, returning a similar hashmap with percentages instead of raw damage amounts.
    static HashMap<String, Float> calculateDamage(HashMap<String, Float> playerDamage) {
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


    // Based on a damage percents hashmap, returns the username string of the player who did the highest damage.
    static String getHighestDamager(HashMap<String, Float> damagePercents) {
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


    // Based on a damage percents hashmap, returns the username string of the player who did the second damage.
    static String getSecondDamager(HashMap<String, Float> damagePercents) {
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


    // Splits the kill payout between the first, and if applicable, second highest damagers.
    static void payDamagers(HashMap<String, Float> damagePercent) {
        // Payout is only split if there is a second player that did more than 20% total damage to the victim.
        if (damagePercent.keySet().size() > 1 && (damagePercent.get(getSecondDamager(damagePercent)) > 0.20)) {
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

        // Otherwise, gives the full payout to the highest damager.
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + getHighestDamager(damagePercent) + " 5.0");
        }
    }
}
