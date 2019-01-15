package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Commands implements CommandExecutor {
    KitPvPEssentials plugin;
    MessageGUI GUI;
    private HashMap<UUID, PlayerData> database;
    private LinkedHashMap<String, KillMessage> killMessages;

    public Commands(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.killMessages = plugin.getKillMessages();
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player sender = (Player) commandSender;

        if (args.length == 0) {
            return false;
        }


        // Player stats command
        if (args[0].equalsIgnoreCase("stats")) {
            if (args.length == 1) {
                inspectPlayer(sender, sender.getPlayerListName());
                return true;
            } else if (args.length == 2) {
                inspectPlayer(sender, args[1]);
                return true;
            }


        // Kill message selection
        } else if (args[0].equalsIgnoreCase("message")) {
            MessageGUI gui = new MessageGUI(plugin);
            gui.openInventory(sender);
            return true;


        } else {
            return false;
        }

        return false;
    }

    // Lookup a player and print the player's stats to the specified receiver.
    private void inspectPlayer(Player sender, String target) {
        Player lookup = Bukkit.getPlayer(target);

        // Creates data for this player if they have none.
        if (!database.containsKey(lookup.getUniqueId())) {
            plugin.createPlayerData(lookup);
        }

        if (lookup.hasPlayedBefore()) {
            PlayerData playerData = database.get(lookup.getUniqueId());
            sender.sendMessage("stats of " + lookup.getDisplayName());
            sender.sendMessage("Kills: " + playerData.getKills());
            sender.sendMessage("Deaths: " + playerData.getDeaths());
            if (playerData.getDeaths() == 0) {
                sender.sendMessage("K/D Radio: " + (playerData.getKills()));
            } else {
                sender.sendMessage("K/D Radio: " + (playerData.getKills() / playerData.getDeaths()));
            }
            sender.sendMessage("Current killsteak: " + playerData.getCurrentStreak());
            sender.sendMessage("Best killstreak: " + playerData.getHighestStreak());

        } else {
            sender.sendMessage("no data for this player");
        }
    }

}
