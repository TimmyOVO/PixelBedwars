package com.github.timmyovo.pixelbedwars.shop.item;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopItem {
    private InventoryItem icon;
    private List<InventoryItem> items;
    private InventoryItem requireItem;
    private String requirePermission;

    public static boolean hasEnoughItems(GamePlayer gamePlayer, InventoryItem inventoryItem) {
        return hasEnoughItems(gamePlayer.getPlayer(), inventoryItem);
    }

    public static boolean hasEnoughItems(Player gamePlayer, InventoryItem inventoryItem) {
        return hasEnoughItems(gamePlayer, inventoryItem.toItemStack());
    }

    public static boolean hasEnoughItems(Player gamePlayer, ItemStack inventoryItem) {
        PlayerInventory inventory = gamePlayer.getInventory();
        int amountSum = 0;
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = inventory.getContents()[i];
            if (content == null) {
                continue;
            }
            if (content.isSimilar(inventoryItem)) {
                amountSum += content.getAmount();
            }
        }
        return amountSum >= inventoryItem.getAmount();
    }

    public static boolean takeItem(Player gamePlayer, ItemStack itemStack) {
        int requireAmount = itemStack.getAmount();
        PlayerInventory inventory = gamePlayer.getInventory();
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack content = inventory.getContents()[i];
            if (content == null) {
                continue;
            }
            if (content.isSimilar(itemStack)) {
                if (content.getAmount() >= requireAmount) {
                    content.setAmount(content.getAmount() - requireAmount);
                    return true;
                } else {
                    requireAmount -= content.getAmount();
                    content.setAmount(0);
                }
            }
        }
        return requireAmount <= 0;
    }

    public static boolean takeItem(GamePlayer gamePlayer, ItemStack itemStack) {
        return takeItem(gamePlayer.getPlayer(), itemStack);
    }

    public static boolean takeItem(GamePlayer gamePlayer, InventoryItem inventoryItem) {
        return takeItem(gamePlayer, inventoryItem.toItemStack());
    }

    public static ShopItem fromString(String string) {
        return FileUtils.GSON.fromJson(string, ShopItem.class);
    }

    public void requestBuyItem(Player gamePlayer) {
        requestBuyItem(PixelBedwars.getPixelBedwars().getBedwarsGame().getBedwarsPlayer(gamePlayer));
    }

    public void requestBuyItem(GamePlayer gamePlayer) {
        if (requirePermission != null) {
            if (!gamePlayer.getPlayer().hasPermission(requirePermission)) {
                gamePlayer.getPlayer().sendMessage("你没有权限购买这个物品");
                return;
            }
        }
        if (hasEnoughItems(gamePlayer, requireItem)) {
            if (takeItem(gamePlayer, requireItem)) {
                items.forEach(inventoryItem1 -> {
                    ItemStack itemStack = inventoryItem1.toItemStack();
                    if (!applyArmor(gamePlayer.getPlayer(), itemStack)) {
                        gamePlayer.getPlayer().getInventory().addItem(itemStack);
                    }
                });
            }
        } else {
            gamePlayer.getPlayer().sendMessage("没有足够的物品购买!");
        }
    }

    public boolean applyArmor(Player player, ItemStack itemStack) {
        PlayerInventory inventory = player.getInventory();
        if (itemStack.getType().name().contains("HELMET")) {
            ItemStack helmet = inventory.getHelmet();
            if (helmet != null) {

            }
            inventory.setHelmet(itemStack);
            return true;
        }
        if (itemStack.getType().name().contains("CHESTPLATE")) {
            inventory.setChestplate(itemStack);
            return true;
        }
        if (itemStack.getType().name().contains("LEGGINGS")) {
            inventory.setLeggings(itemStack);
            return true;
        }
        if (itemStack.getType().name().contains("BOOTS")) {
            inventory.setBoots(itemStack);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return FileUtils.GSON.toJson(this);
    }
}
