package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.shop.item.ShopTeamItem;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class TeamShopGui implements Listener {

    public static final String TEAM_SHOP_ITEM_KEY = "TEAM_SHOP_ITEM_KEY";

    private Map<Integer, ShopTeamItem> shopTeamItems;
    private BedwarsGame bedwarsGame;
    private Inventory inventory;

    public TeamShopGui(AbstractShop shop) {
        this.bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
        this.inventory = Bukkit.createInventory(null, 54, shop.getDisplayName());
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i + 18, new ItemStack(Material.STAINED_GLASS_PANE));
        }
    }

    private void applyItems(Player player) {
        shopTeamItems.forEach((i, s) -> {
            ItemStack itemStack = s.getIcon().toItemStack();
            net.minecraft.server.v1_8_R3.ItemStack asNMSCopy = CraftItemStack.asNMSCopy(itemStack);
            if (asNMSCopy.getTag() == null) {
                asNMSCopy.setTag(new NBTTagCompound());
            }
            asNMSCopy.getTag().setString(TEAM_SHOP_ITEM_KEY, s.toString());
            inventory.setItem(i, itemStack);
        });
    }

    public void show(Player gamePlayer) {
        applyItems(gamePlayer);
        gamePlayer.openInventory(inventory);
    }

    private void notifyClick(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        ItemStack currentItem = inventoryClickEvent.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        net.minecraft.server.v1_8_R3.ItemStack asNmsCopy = CraftItemStack.asNMSCopy(currentItem);
        NBTTagCompound tag = asNmsCopy.getTag();
        if (tag == null) {
            return;
        }
        if (tag.hasKey(TEAM_SHOP_ITEM_KEY)) {
            ShopTeamItem shopTeamItem = ShopTeamItem.fromString(tag.getString(TEAM_SHOP_ITEM_KEY));
            shopTeamItem.requestBuyItem(((Player) inventoryClickEvent.getWhoClicked()));
        }
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent inventoryClickEvent) {
        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory == inventoryClickEvent.getWhoClicked().getInventory()) {
            if (inventoryClickEvent.getSlot() <= 39 && inventoryClickEvent.getSlot() >= 36) {
                inventoryClickEvent.setCancelled(true);
            }
            return;
        }
        if (clickedInventory.getName().equals(PixelBedwars.getPixelBedwars().getLanguage().getTeamShopDisplayName())) {
            inventoryClickEvent.setCancelled(true);
            notifyClick(inventoryClickEvent);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent playerInteractEntityEvent) {
        Entity rightClicked = playerInteractEntityEvent.getRightClicked();
        if (rightClicked == null) {
            return;
        }
        if (rightClicked.getType() == bedwarsGame.getGameSetting().getTeamGuiEntityType()) {
            show(playerInteractEntityEvent.getPlayer());
        }
    }

    public static enum TeamItemType {
        SHARPNESS_ENCHANTMENT, TEAM_PROECTION_ENCHANTMENT,
    }
}
