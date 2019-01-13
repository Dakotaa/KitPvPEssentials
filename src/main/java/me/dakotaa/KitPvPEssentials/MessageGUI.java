package me.dakotaa.KitPvPEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class MessageGUI implements Listener {
    KitPvPEssentials plugin;
    private HashMap<UUID, PlayerData> database;
    private HashMap<String, KillMessage> killMessages;

    private final Inventory inv;

    public MessageGUI(KitPvPEssentials plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
        this.killMessages = plugin.getKillMessages();
        inv = Bukkit.createInventory(null, 9, "Kill Messages");
    }

    // Create gui item with name and lore
    public ItemStack createGuiItem(String name, ArrayList<String> desc, Material mat) {
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta iMeta = i.getItemMeta();
        iMeta.setDisplayName(name);
        iMeta.setLore(desc);
        i.setItemMeta(iMeta);
        return i;
    }

    // Creates itemstacks to put into GUI
    public void initializeItems() {
        for (String label : killMessages.keySet()) {
            inv.addItem(createGuiItem(ChatColor.translateAlternateColorCodes('&', "&f" + label), new ArrayList<String>(Arrays.asList(ChatColor.translateAlternateColorCodes('&', killMessages.get(label).getMessage().replace("%killer%", "PLAYER").replace("%victim%", "PLAYER").replace("%killstreak%", "")))), Material.PAPER));
        }
    }

    public void openInventory(Player p) {
        initializeItems();
        p.openInventory(inv);
        return;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String invName = e.getInventory().getName();
        if (!invName.equals(inv.getName())) {
            return;
        }

        if (e.getClick().equals(ClickType.NUMBER_KEY)) {
            e.setCancelled(true);
        }

        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) {
            return;
        }

        Bukkit.getLogger().info("Slot: " + e.getRawSlot());

        // Checks through the existing kill message orders to see if the inventory slot clicked matches with any kill message. If it does, sets the player's kill message.
        for (String label : killMessages.keySet()) {
            if (killMessages.get(label).getOrder() == e.getRawSlot()+1) {
                database.get(p.getUniqueId()).setKillMessage(killMessages.get(label).getMessage());
                p.sendMessage("&eYou have set your kill message to " + killMessages.get(label).getMessage());
                break;
            }
        }
    }

}
