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

public class EventListener implements Listener {

    KitPvPEssentials plugin;
    private HashMap<UUID, PlayerData> database;
    private LinkedHashMap<String, KillMessage> killMessages;

    public EventListener(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.killMessages = plugin.getKillMessages();
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

        victimData.setCurrentStreak(0);

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
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', killerData.getKillMessage().replace("%killer%", killer.getDisplayName()).replace("%victim%", victim.getDisplayName()).replace("%killstreak%", killStreakMessage(killer))));
        } else {
            // Does not display a kill message if  the player is not killed in PvP.
            e.setDeathMessage(null);
        }
    }

    // Returns the correct killstreak message string depending on the given player's current killstreak.
    // TODO: load kill streak tiers, messages, and rewards from config
    private String killStreakMessage(Player player) {
        PlayerData playerData = database.get(player.getUniqueId());
        if (playerData.getCurrentStreak() > 5) {
            return player.getDisplayName() + " has a 5 killstreak!";
        }

        return "";
    }

}
