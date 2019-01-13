package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class EventListener implements Listener {

    KitPvPEssentials plugin;
    private HashMap<UUID, PlayerData> database;
    private HashMap<String, KillMessage> killMessages;

    public EventListener(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.killMessages = plugin.getKillMessages();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID UUID = player.getUniqueId();
        if (!database.containsKey(UUID)) {
            database.put(UUID, new PlayerData(UUID, player.getPlayerListName(), killMessages.get("default").getMessage(), 0, 0, 0, 0));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        UUID victimUUID = victim.getUniqueId();
        PlayerData victimData = database.get(victimUUID);

        if (!database.containsKey(victimUUID)) {
            database.put(victimUUID, new PlayerData(victimUUID, victim.getPlayerListName(), killMessages.get("default").getMessage(), 0, 0, 0, 0));
        }

        victimData.increaseDeaths();
        if (victimData.getCurrentStreak() > victimData.getHighestStreak()) {
            victimData.setHighestStreak(victimData.getCurrentStreak());
        }
        victimData.setCurrentStreak(0);

        if (victim.getKiller() instanceof Player) {
            Player killer = victim.getKiller();
            UUID killerUUID = killer.getUniqueId();
            PlayerData killerData = database.get(killerUUID);

            if (!database.containsKey(killerUUID)) {
                database.put(killerUUID, new PlayerData(killerUUID, killer.getPlayerListName(), killMessages.get("default").getMessage(), 0, 0, 0, 0));
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

    // function to decide which killstreak message to use.
    private String killStreakMessage(Player player) {
        PlayerData playerData = database.get(player.getUniqueId());
        if (playerData.getCurrentStreak() > 5) {
            return player.getDisplayName() + " has a 5 killstreak!";
        }

        return "";
    }

}
