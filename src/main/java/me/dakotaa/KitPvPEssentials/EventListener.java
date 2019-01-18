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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class EventListener implements Listener {

    KitPvPEssentials plugin;
    private HashMap<UUID, PlayerData> database;
    private HashMap<Integer, KillStreak> killStreaks;
    static DecimalFormat round = new DecimalFormat("##.00");

    public EventListener(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
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

            double hp = damaged.getHealth();
            double dmg = e.getFinalDamage();

            if (hp - dmg <= 0) {
                dmg = hp;
            }

            database.get(damaged.getUniqueId()).addDamage(attacker.getName(), (float) dmg);
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

        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            UUID killerUUID = killer.getUniqueId();
            PlayerData killerData = database.get(killerUUID);

            if (!database.containsKey(killerUUID)) {
                plugin.createPlayerData(victim);
            }

            try {
                HashMap<String, Float> damagePercents = SplitKill.calculateDamage(victimData.getDamageReceived());
                String highestDamage = SplitKill.getHighestDamager(damagePercents);
                String secondHighest = SplitKill.getSecondDamager(damagePercents);

                if (victimData.getOnStreak()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&4&l%killer% &c&ljust ended &4&l%victim%'s &c&l%streak% player killstreak!").replace("%killer%", killer.getName()).replace("%victim%", victim.getName()).replace("%streak%", valueOf(victimData.getCurrentStreak())));
                }

                // Handles kill messaging - if the person who got the last hit did the most damage, broadcast their kill message. If they did the second most and more than 20%, announce an assist. If they did less, announce a finish.
                if (killer.getName().equalsIgnoreCase(highestDamage)) {
                    e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', killerData.getKillMessage().replace("%killer%", killer.getName()).replace("%damage%", round.format(damagePercents.get(killer.getName())*100)).replace("%victim%", victim.getName()).replace("%killstreak%", killStreakMessage(killer, killerData.getCurrentStreak()))));
                } else if (killer.getName().equalsIgnoreCase(secondHighest) && damagePercents.get(killer.getName()) > 0.2) {
                    e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', ("&e&l%assister% &7&lassisted &a&l%killer% &7&lin killing &f%victim%&7&l. [&e&l%damage%% DMG&7&l]").replace("%killer%", highestDamage).replace("%damage%", round.format(damagePercents.get(killer.getName())*100)).replace("%assister%", secondHighest).replace("%victim%", victim.getName()).replace("%killstreak%", killStreakMessage(killer, killerData.getCurrentStreak()))));
                } else {
                    e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', ("&6&l%stealer% &7&lfinished &a&l%killer%&7&l's kill on &c&l%victim%&7&l. [&6&l%damage%% DMG&7&l]").replace("%stealer%", killer.getName()).replace("%victim%", victim.getName()).replace("%damage%", round.format(damagePercents.get(killer.getName())*100)).replace("%killer%", highestDamage).replace("%killstreak%", killStreakMessage(killer, killerData.getCurrentStreak()))));
                }

                SplitKill.ProcessKill(victim, killer,  database, killStreaks);

            } catch (Exception er) {
                Bukkit.getLogger().info("Error splitting kill: " + er.toString());
            }

            // Broadcast that the victim's streak has been ended by the killer is the victim is on a streak.
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
    private String killStreakMessage(Player player, int streak) {
        if (streakHit(streak)) {
            PlayerData playerData = database.get(player.getUniqueId());
            return getKillStreak(streak).getMessage().replace("%player%", player.getName());
        } else {
            return "";
        }
    }

}
