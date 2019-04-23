package com.github.timmyovo.pixelbedwars.shop;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.shop.category.ShopCategory;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
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

import java.util.List;

public class TeamShopGui implements Listener {
    public static final String SHOP_ITEM_KEY = "SHOP_ITEM";
    public static final String CATEGORY_KEY = "CATEGORY";
    private List<ShopTeamItem> shopTeamItems;
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
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.IRON_SWORD))
                .setDisplayName("§c锋利附魔")
                .addLore("§7你放所有人员的剑和斧将永久获得锋利Ⅰ附魔!")
                .addLore("  ")
                .addLore("§7花费:§b8钻石")
                .addLore("  ");
        if (ShopItem.hasEnoughItems(player, InventoryItem.builder()
                .itemstackData(new ItemStack(Material.DIAMOND, 8).serialize())
                .build())) {
            itemFactory.addLore("§a点击购买");
        } else {
            itemFactory.addLore("§c你没有足够的钻石");
        }
        this.inventory.setItem(10, itemFactory
                .pack());
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
        if (tag.hasKey(CATEGORY_KEY)) {
            ShopCategory shopCategory = ShopCategory.fromString(tag.getString(CATEGORY_KEY));
            shopCategory.applyToInventory(inventory);
            return;
        }
        if (tag.hasKey(SHOP_ITEM_KEY)) {
            String string = tag.getString(SHOP_ITEM_KEY);
            ShopItem shopItem = ShopItem.fromString(string);
            shopItem.requestBuyItem(player);
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
        if (clickedInventory.getName().equals(shop.getDisplayName())) {
            inventoryClickEvent.setCancelled(true);
            notifyClick(inventoryClickEvent);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent playerInteractEntityEvent) {
        Entity rightClicked = playerInteractEntityEvent.getRightClicked();
        if (rightClicked.getType() == shop.getEntityType()) {
            playerInteractEntityEvent.setCancelled(true);
            show(playerInteractEntityEvent.getPlayer());
        }
    }
}
