package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class KitPvPEssentials extends JavaPlugin {

    private HashMap<UUID, PlayerData> database;
    private LinkedHashMap<String, KillMessage> killMessages;
    private HashMap<Integer, KillStreak> killStreaks;
    private PluginFile playerDataFile;
    private MessageGUI gui;

    @Override
    public void onEnable() {

        // Hashmap to store player data by UUID.
        database = new HashMap<UUID, PlayerData>();
        killStreaks = new HashMap<Integer, KillStreak>();

        // Kill message hashmap. Kill message data loaded into here from the config.yml.
        // Linked hashmap so order of hashmap matches order of config so the GUI works correctly.
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

        loadKillStreaks();

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

    // Load the kill message info from the config, placing each configured kill message into the killMessages hashmap.
    private void loadKillMessages() {
        for (String label : getConfig().getConfigurationSection("KillMessages").getKeys(false)) {
            String path = "KillMessages." + label;
            killMessages.put(label, new KillMessage(getConfig().getString(path + ".label"), getConfig().getString(path + ".message"), getConfig().getInt(path + ".order")));
            //getLogger().info(getConfig().getString("KillMessages." + label));
        }
    }

    // Load killstreaks from config.
    private void loadKillStreaks() {
        for (String label : getConfig().getConfigurationSection("KillStreaks").getKeys(false)) {
            try {
                String path = "KillStreaks." + label;
                getLogger().info("Read data");
                killStreaks.put(getConfig().getInt(path + ".kills"), new KillStreak(getConfig().getInt(path + ".kills"), getConfig().getString(path + ".message"), new ArrayList<String>(getConfig().getStringList(path + ".commands"))));
            } catch (Exception e) {
                getLogger().info("Failed to load killstreak: " + e.toString());
            }
        }
    }

    // Write the player data for each player in the database hashmap to the PlayerData.yml file.
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

    // Used to add empty player data to the database hashmap to initialize players without any data.
    public void createPlayerData(Player p) {
        database.put(p.getUniqueId(), new PlayerData(p.getUniqueId(), p.getPlayerListName(), killMessages.get("default").getMessage(), 0, 0, 0, 0));
    }

    public HashMap<UUID, PlayerData> getDatabase() {
        return database;
    }

    public HashMap<Integer, KillStreak> getKillStreaks() {
        return killStreaks;
    }

    public LinkedHashMap<String, KillMessage> getKillMessages() {
        return killMessages;
    }

    public MessageGUI getMessageGUI() {
        return gui;
    }



}

