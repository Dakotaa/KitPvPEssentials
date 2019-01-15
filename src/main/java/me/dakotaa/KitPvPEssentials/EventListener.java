package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class EventListener implements Listener {

    KitPvPEssentials plugin;
    private HashMap<UUID, PlayerData> database;
    private HashMap<Integer, KillStreak> killStreaks;
    private LinkedHashMap<String, KillMessage> killMessages;

    public EventListener(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.killMessages = plugin.getKillMessages();
        killStreaks = plugin.getKillStreaks();
    }

    // Create PlayerData object for player on login if they do not have any.
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID UUID = player.getUniqueId();
        if (!database.containsKey(UUID)) {
            plugin.createPlayerData(player);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player attacker = ((Player) e.getDamager()).getPlayer();
            Player damaged = ((Player) e.getEntity()).getPlayer();

            double dmg = e.getFinalDamage();

            database.get(damaged.getUniqueId()).addDamage(attacker.getName(), (float) dmg);
            Bukkit.getLogger().info(dmg + " damage done to " + damaged.getName() + " by " + attacker.getName());
            Bukkit.getLogger().info("Total damage done to " + damaged.getName() + " by " + attacker.getName() + ": " + database.get(damaged.getUniqueId()).getDamageByPlayer(attacker.getName()));
        }
    }

    // Handles the changing of stats and sending death message when a player is killed in PvP.
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        UUID victimUUID = victim.getUniqueId();
        PlayerData victimData = database.get(victimUUID);

        Bukkit.getLogger().info(victimData.getKillMessage());

        try {
            if (!database.containsKey(victimUUID)) {
                plugin.createPlayerData(victim);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().info("Failed to create player data");
        }

        try { victimData.increaseDeaths(); } catch (Exception ex) { Bukkit.getLogger().info("Failed to increase deaths"); }

        if (victimData.getCurrentStreak() > victimData.getHighestStreak()) {
            victimData.setHighestStreak(victimData.getCurrentStreak());
        }


        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            UUID killerUUID = killer.getUniqueId();
            PlayerData killerData = database.get(killerUUID);

            if (!database.containsKey(killerUUID)) {
                plugin.createPlayerData(victim);
            }

            // Display the killer's custom kill message in chat.
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', killerData.getKillMessage().replace("%killer%", killer.getDisplayName()).replace("%victim%", victim.getDisplayName()).replace("%killstreak%", killStreakMessage(killer, killerData.getCurrentStreak()))));

            // If the player hit a streak on this kill, set their streak boolean to true.
            if (streakHit(killerData.getCurrentStreak())) {
                killerData.setOnStreak(true);
            }

            try {
                HashMap<String, Float> damagePercents = SplitKill.calculateDamage(victimData.getDamageReceived());
                Bukkit.getLogger().info("Got damage percents");
                ArrayList<String> pair = SplitKill.getHighestDamagers(SplitKill.calculateDamage(damagePercents));
                Bukkit.getLogger().info("Got pairs");

                try {
                    Bukkit.getLogger().info(pair.get(0));
                    Bukkit.getLogger().info(valueOf(damagePercents.get(pair.get(0))));

                    if (pair.get(1) != "") {
                        Bukkit.getLogger().info("pair1: " + pair.get(1));
                        Bukkit.getLogger().info(valueOf(damagePercents.get(pair.get(1))));
                        payDamagers(pair.get(0), damagePercents.get(pair.get(0)), pair.get(1), damagePercents.get(pair.get(1)));
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + killer.getName() + " 2.5");
                    }
                    Bukkit.getLogger().info("Paid damagers");
                    for (String p : damagePercents.keySet()) {
                        if (Bukkit.getPlayer(p) != null) {
                            Player pl = Bukkit.getPlayer(p);
                            pl.sendMessage("You did " + damagePercents.get(p) * 100 + "% damage to " + victim.getName());
                        }
                    }

                    if (Bukkit.getPlayer(pair.get(1)) != null) {
                        PlayerData firstData = database.get(Bukkit.getPlayer(pair.get(1)).getUniqueId());
                        Bukkit.getLogger().info("Increased kills of " + pair.get(1));
                        firstData.increaseKills();
                        firstData.increaseStreak();
                    }

                    if (Bukkit.getPlayer(pair.get(0)) != null) {
                        PlayerData secondData = database.get(Bukkit.getPlayer(pair.get(0)).getUniqueId());
                        Bukkit.getLogger().info("Increased assists of " + pair.get(0));
                        secondData.increaseAssists();
                    }

                } catch (Exception er) {
                    Bukkit.getLogger().info("Error paying damagers");
                }
            } catch (Exception er) {
                Bukkit.getLogger().info("Error splitting payout: " + er.toString());
            }
            // Execute commands when the player hits a streak.
            if (streakHit(killerData.getCurrentStreak())) {
                for (String command : getKillStreak(killerData.getCurrentStreak()).getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", killer.getName()).replace("%killed%", victim.getName()).replace("%streak%", valueOf(killerData.getCurrentStreak())));
                }
            }

            /*
            // Increase the kills and killstreak of the killer. If their current killstreak is their highest, updates their highest killstreak.
            if ()
            killerData.increaseKills();
            killerData.increaseStreak();
            if (killerData.getCurrentStreak() > killerData.getHighestStreak()) {
                killerData.setHighestStreak(killerData.getCurrentStreak());
            }
            */
            // Broadcast that the victim's streak has been ended by the killer is the victim is on a streak.
            if (victimData.getOnStreak()) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l%killer% &c&ljust ended &4&l%victim%'s &c&l%streak% player killstreak!").replace("%killer%", killer.getName()).replace("%victim%", victim.getName()).replace("%streak%", valueOf(victimData.getCurrentStreak())));
            }

        } else {
            // Does not display a kill message if  the player is not killed in PvP.
            e.setDeathMessage(null);
        }
        victimData.setCurrentStreak(0);
        victimData.setOnStreak(false);
        victimData.resetDamage();
    }

    private void payDamagers(String player1, Float player1dmg, String player2, Float player2dmg) {
        Float payout = 2.5f;
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
    }

    private Boolean streakHit(int streak) {
        return (killStreaks.keySet().contains(streak));
    }

    private KillStreak getKillStreak(int streak) {
        if (killStreaks.keySet().contains(streak)) {
            return killStreaks.get(streak);
        } else {
            return null;
        }
    }

    // Returns the killstreak message to use if the player hits a valid killstreak
    // TODO: load kill streak tiers, messages, and rewards from config
    private String killStreakMessage(Player player, int streak) {
        if (streakHit(streak)) {
            PlayerData playerData = database.get(player.getUniqueId());
            return getKillStreak(streak).getMessage().replace("%player%", player.getName());
        } else {
            return "";
        }
    }

}
