package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.database.PlayerQuickShopEntryModel;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.shop.category.ShopCategory;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopGui implements Listener {
    public static final String SHOP_ITEM_KEY = "SHOP_ITEM";
    public static final String CATEGORY_KEY = "CATEGORY";
    protected PlayerShop shop;
    protected BedwarsGame bedwarsGame;
    protected Inventory inventory;

    public ShopGui(PlayerShop shop) {
        this.shop = shop;
        this.bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
        this.inventory = Bukkit.createInventory(null, 54, shop.getDisplayName());

    }

    protected void applyCategory(Player player) {
        inventory.clear();
        shop.getCategoryItems().forEach((i, shopCategory) -> {
            ItemStack categoryItem = shopCategory.getIcon().toItemStack();
            net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(categoryItem);
            if (nmsCopy.getTag() == null) {
                nmsCopy.setTag(new NBTTagCompound());
            }
            nmsCopy.getTag().setString(CATEGORY_KEY, shopCategory.toString());
            inventory.setItem(i, CraftItemStack.asBukkitCopy(nmsCopy));
        });
        ShopCategory shopCategory = shop.getCategoryItems().get(0).clone();
        PlayerQuickShopEntryModel.db().find(PlayerQuickShopEntryModel.class)
                .where()
                .eq("owner", player.getUniqueId())
                .findList()
                .forEach(playerQuickShopEntryModel -> {
                    shopCategory.getShopItemMap().put(playerQuickShopEntryModel.getSlotId(), ShopItem.fromString(playerQuickShopEntryModel.getShopItem()));
                });
        shopCategory.applyToInventory(inventory);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i + 9, new ItemStack(Material.STAINED_GLASS_PANE));
        }
    }

    public void show(Player gamePlayer) {
        applyCategory(gamePlayer);
        gamePlayer.openInventory(inventory);
    }

    protected void notifyClick(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        ItemStack currentItem = inventoryClickEvent.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        net.minecraft.server.v1_8_R3.ItemStack asNmsCopy = CraftItemStack.asNMSCopy(currentItem);
        if (asNmsCopy == null) {
            return;
        }
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
            if (inventoryClickEvent.isShiftClick()) {
                ConfigQuickShopGui configQuickShopGui = PixelBedwars.getPixelBedwars().getConfigQuickShopGui();
                configQuickShopGui.setPlayerShopItem(player, shopItem);
                configQuickShopGui.show(player);
                return;
            } else if (inventoryClickEvent.isRightClick()) {
                PlayerQuickShopEntryModel.setPlayerQuickShopEntry(player.getUniqueId(), inventoryClickEvent.getSlot(), null);
                applyCategory(player);
                return;
            }
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
