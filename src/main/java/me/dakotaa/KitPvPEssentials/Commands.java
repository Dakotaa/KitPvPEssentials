package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.lang.String.valueOf;

public class Commands implements CommandExecutor {
    KitPvPEssentials plugin;
    MessageGUI GUI;
    private HashMap<UUID, PlayerData> database;
    static DecimalFormat round = new DecimalFormat("##.00");

    public Commands(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-header")).replace("%player%", lookup.getName()));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-kills")).replace("%kills%", valueOf(playerData.getKills())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-deaths")).replace("%deaths%", valueOf(playerData.getDeaths())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-assists")).replace("%assists%", valueOf(playerData.getAssists())));
            if (playerData.getDeaths() == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-kd")).replace("%ratio%", valueOf(playerData.getKills()/playerData.getDeaths())));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-kd")).replace("%ratio%", valueOf(round.format(playerData.getKills()/playerData.getDeaths()))));
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-kd")).replace("%ratio%", valueOf(playerData.getKills())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-streak")).replace("%streak%", valueOf(playerData.getCurrentStreak())));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-streak-best")).replace("%beststreak%", valueOf(playerData.getHighestStreak())));
            if (playerData.getDeaths() == 0) {
                sender.sendMessage("K/D Radio: " + (playerData.getKills()));
            } else {
                sender.sendMessage("K/D Radio: " + round.format((playerData.getKills() / playerData.getDeaths())));
            }

        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', KitPvPEssentials.messages.get("stats-no-data")));
        }
    }

}
