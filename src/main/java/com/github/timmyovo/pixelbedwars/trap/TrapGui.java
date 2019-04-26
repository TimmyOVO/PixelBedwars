package com.github.timmyovo.pixelbedwars.trap;

import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.shop.TeamShoppingProperties;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrapGui implements Listener {
    private Inventory inventory;

    public TrapGui() {
        this.inventory = Bukkit.createInventory(null, 27, "排列陷阱");
    }

    public void apply(Player player) {
        inventory.setItem(10, getBlindIcon(player));
        inventory.setItem(11, getSpeedTrapIcon(player));
        inventory.setItem(12, getNotificationIcon(player));
        inventory.setItem(13, getDiaSpeedTrapIcon(player));
    }

    public void notifyClick(InventoryClickEvent inventoryClickEvent) {
        Player whoClicked = (Player) inventoryClickEvent.getWhoClicked();
        GameTeam playerTeam = PixelBedwars.getPixelBedwars().getBedwarsGame().getPlayerTeam(whoClicked);
        if (inventoryClickEvent.getSlot() == 10) {
            ItemStack inventoryItem = new ItemStack(Material.DIAMOND);
            TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
            if (teamShoppingProperties.getBlindTrap0() > 0) {
                inventoryItem.setAmount(teamShoppingProperties.getBlindTrap0());
            }
            if (teamShoppingProperties.getBlindTrap0() >= 3) {
                return;
            }
            if (ShopItem.hasEnoughItems(whoClicked, inventoryItem)) {
                ShopItem.takeItem(whoClicked, inventoryItem);
                teamShoppingProperties.setBlindTrap0(teamShoppingProperties.getBlindTrap0() + 1);
                teamShoppingProperties.setBlindTrap(true);
                whoClicked.sendMessage("成功购买");
            } else {
                whoClicked.sendMessage("无法购买金钱不够");
            }
            return;
        }
        if (inventoryClickEvent.getSlot() == 11) {
            ItemStack inventoryItem = new ItemStack(Material.DIAMOND);
            TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
            if (teamShoppingProperties.getSpeedupTrap0() > 0) {
                inventoryItem.setAmount(teamShoppingProperties.getSpeedupTrap0());
            }
            if (teamShoppingProperties.getBlindTrap0() >= 3) {
                return;
            }
            if (ShopItem.hasEnoughItems(whoClicked, inventoryItem)) {
                ShopItem.takeItem(whoClicked, inventoryItem);
                teamShoppingProperties.setSpeedupTrap0(teamShoppingProperties.getSpeedupTrap0() + 1);
                teamShoppingProperties.setSpeedupTrap(true);
                whoClicked.sendMessage("成功购买");
            } else {
                whoClicked.sendMessage("无法购买金钱不够");
            }
            return;
        }
        if (inventoryClickEvent.getSlot() == 12) {
            ItemStack inventoryItem = new ItemStack(Material.DIAMOND);
            TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
            if (teamShoppingProperties.getNotificationTrap0() > 0) {
                inventoryItem.setAmount(teamShoppingProperties.getNotificationTrap0());
            }
            if (teamShoppingProperties.getNotificationTrap0() >= 3) {
                return;
            }
            if (ShopItem.hasEnoughItems(whoClicked, inventoryItem)) {
                ShopItem.takeItem(whoClicked, inventoryItem);
                teamShoppingProperties.setNotificationTrap0(teamShoppingProperties.getNotificationTrap0() + 1);
                teamShoppingProperties.setNotificationTrap(true);
                whoClicked.sendMessage("成功购买");
            } else {
                whoClicked.sendMessage("无法购买金钱不够");
            }
            return;
        }
        if (inventoryClickEvent.getSlot() == 13) {
            ItemStack inventoryItem = new ItemStack(Material.DIAMOND);
            TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
            if (teamShoppingProperties.getDigSpeedTrap0() > 0) {
                inventoryItem.setAmount(teamShoppingProperties.getDigSpeedTrap0());
            }
            if (teamShoppingProperties.getDigSpeedTrap0() >= 3) {
                return;
            }
            if (ShopItem.hasEnoughItems(whoClicked, inventoryItem)) {
                ShopItem.takeItem(whoClicked, inventoryItem);
                teamShoppingProperties.setDigSpeedTrap0(teamShoppingProperties.getDigSpeedTrap0() + 1);
                teamShoppingProperties.setDigSpeedTrap(true);
                whoClicked.sendMessage("成功购买");
            } else {
                whoClicked.sendMessage("无法购买金钱不够");
            }
            return;
        }
    }

    private ItemStack getDiaSpeedTrapIcon(Player player) {
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.IRON_PICKAXE));
        itemFactory.setDisplayName(ChatColor.RED + "挖掘疲劳!");
        itemFactory.addLore(ChatColor.GRAY + "当有敌人进入床附近时使床附近的敌人进入挖掘疲劳状态");
        return itemFactory.pack();
    }

    private ItemStack getSpeedTrapIcon(Player player) {
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.FEATHER));
        itemFactory.setDisplayName(ChatColor.RED + "反击陷阱!");
        itemFactory.addLore(ChatColor.GRAY + "当有敌人进入床附近时使床附近的队友获得速度效果");
        return itemFactory.pack();
    }

    private ItemStack getNotificationIcon(Player player) {
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.REDSTONE_TORCH_ON));
        itemFactory.setDisplayName(ChatColor.RED + "报警陷阱!");
        itemFactory.addLore(ChatColor.GRAY + "当有敌人进入床附近时使敌人缓慢和失明");
        return itemFactory.pack();
    }

    private ItemStack getBlindIcon(Player player) {
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.TRIPWIRE_HOOK));
        itemFactory.setDisplayName(ChatColor.RED + "这是个陷阱!");
        itemFactory.addLore(ChatColor.GRAY + "当有敌人进入床附近时使敌人缓慢和失明");
        return itemFactory.pack();
    }

    public void show(Player player) {
        apply(player);
        player.openInventory(this.inventory);
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent inventoryClickEvent) {
        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory.getName().equals("排列陷阱")) {
            inventoryClickEvent.setCancelled(true);
            notifyClick(inventoryClickEvent);
        }
    }
}
