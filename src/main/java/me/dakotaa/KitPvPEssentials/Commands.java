package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Commands implements CommandExecutor {
    KitPvPEssentials plugin;
    MessageGUI GUI;
    private HashMap<UUID, PlayerData> database;
    private HashMap<String, KillMessage> killMessages;

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
        if (args[0].equalsIgnoreCase("stats")) {
            if (args.length == 1) {
                inspectPlayer(sender, sender.getPlayerListName());
                return true;
            } else if (args.length == 2) {
                inspectPlayer(sender, args[1]);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("message")) {
            MessageGUI gui = new MessageGUI(plugin);
            gui.openInventory(sender);
        } else {
            return false;
        }
        return false;
    }

    private void inspectPlayer(Player sender, String target) {
        Player lookup = Bukkit.getPlayer(target);
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
            sender.sendMessage("Best killstreak: " + playerData.getHighestStreak());

        } else {
            sender.sendMessage("no data for this player");
        }
    }

}
