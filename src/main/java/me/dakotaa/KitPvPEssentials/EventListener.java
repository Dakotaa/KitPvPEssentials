package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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

            // Increase the kills and killstreak of the killer. If their current killstreak is their highest, updates their highest killstreak.
            killerData.increaseKills();
            killerData.increaseStreak();
            if (killerData.getCurrentStreak() > killerData.getHighestStreak()) {
                killerData.setHighestStreak(killerData.getCurrentStreak());
            }

            // Display the killer's custom kill message in chat.
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', killerData.getKillMessage().replace("%killer%", killer.getDisplayName()).replace("%victim%", victim.getDisplayName()).replace("%killstreak%", killStreakMessage(killer, killerData.getCurrentStreak()))));

            // If the player hit a streak on this kill, set their streak boolean to true.
            if (streakHit(killerData.getCurrentStreak())) {
                killerData.setOnStreak(true);
            }

            // Execute commands when the player hits a streak.
            if (streakHit(killerData.getCurrentStreak())) {
                for (String command : getKillStreak(killerData.getCurrentStreak()).getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", killer.getName()).replace("%killed%", victim.getName()).replace("%streak%", valueOf(killerData.getCurrentStreak())));
                }
            }

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
