package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class KitPvPEssentials extends JavaPlugin {

    private HashMap<UUID, PlayerData> database;
    private LinkedHashMap<String, KillMessage> killMessages;
    private PluginFile playerDataFile;
    private MessageGUI gui;

    @Override
    public void onEnable() {

        database = new HashMap<UUID, PlayerData>();
        killMessages = new LinkedHashMap<String, KillMessage>();
        gui = new MessageGUI(this);

        // Load or create playerdata and config.
        try {
            playerDataFile = new PluginFile(this, "PlayerData.yml", "PlayerData.yml");
        } catch (Exception e) {
            getLogger().info("Failed to load player data file: " + e.toString());
        }


        saveDefaultConfig();


        // Initialize event listener and commands
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getPluginManager().registerEvents(new MessageGUI(this), this);
        this.getCommand("kitpvpe").setExecutor(new Commands(this));


        // Scheduler to write player data every minute.
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                writePlayerData();
            }
        }, 20L, 1200L);

        loadKillMessages();
        loadPlayerData(playerDataFile);

        getLogger().info("Plugin enabled.");
    }

    public void onDisable() {
        getLogger().info("Plugin disabled.");
        writePlayerData();
    }

    // Load player data into the database hashmap from the PlayerData.yml file.
    private void loadPlayerData(PluginFile data) {
        for (String UUID : data.getKeys(false)) {
            try {
                database.put(java.util.UUID.fromString(UUID), new PlayerData(java.util.UUID.fromString(UUID), data.getString(UUID + ".username"), killMessages.get("default").getMessage(), data.getInt(UUID + ".kills"), data.getInt(UUID + ".deaths"), data.getInt(UUID + "currentStreak"), data.getInt(UUID + ".highestStreak")));
            } catch (Exception e) {
                getLogger().info("Failed to load player data of " + UUID + ": " + e.toString());
            }
        }
    }

    private void loadKillMessages() {
        for (String label : getConfig().getConfigurationSection("KillMessages").getKeys(false)) {
            String path = "KillMessages." + label;
            killMessages.put(label, new KillMessage(getConfig().getString(path + ".label"), getConfig().getString(path + ".message"), getConfig().getInt(path + ".order")));
            //getLogger().info(getConfig().getString("KillMessages." + label));
        }
    }

    private void writePlayerData() {
        for (UUID UUID: database.keySet()) {
            String UUIDs = UUID.toString();
            try {
                playerDataFile.set(UUIDs, UUIDs);
            } catch (Exception e) {
                getLogger().info("Failed to set UUID: " + e.toString());
            }

            try {
                playerDataFile.set(UUID + ".username", Bukkit.getOfflinePlayer(UUID).getName());
                playerDataFile.set(UUID + ".kills", database.get(UUID).getKills());
                playerDataFile.set(UUID + ".deaths", database.get(UUID).getDeaths());
                playerDataFile.set(UUID + ".currentStreak", database.get(UUID).getCurrentStreak());
                playerDataFile.set(UUID + ".highestStreak", database.get(UUID).getHighestStreak());
            } catch (Exception e) {
                getLogger().info("Failed to set player data: " + e.toString());
            }
        }

        playerDataFile.save();
        getLogger().info("Player data saved.");
    }

    public void createPlayerData(Player p) {
        database.put(p.getUniqueId(), new PlayerData(p.getUniqueId(), p.getPlayerListName(), killMessages.get("default").getMessage(), 0, 0, 0, 0));
    }

    public HashMap<UUID, PlayerData> getDatabase() {
        return database;
    }

    public LinkedHashMap<String, KillMessage> getKillMessages() {
        return killMessages;
    }

    public MessageGUI getMessageGUI() {
        return gui;
    }


}

