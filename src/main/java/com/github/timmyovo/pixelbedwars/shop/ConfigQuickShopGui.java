package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.database.PlayerQuickShopEntryModel;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfigQuickShopGui extends ShopGui {
    private ShopItem shopItem;

    public ConfigQuickShopGui(ShopItem shopItem) {
        this.shopItem = shopItem;
    }

    @Override
    protected void notifyClick(InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getSlot() <= 17) {
            return;
        }
        if (shopItem == null) {
            return;
        }
        PlayerQuickShopEntryModel.setPlayerQuickShopEntry(inventoryClickEvent.getWhoClicked().getUniqueId(), inventoryClickEvent.getSlot(), shopItem.toString());
        applyCategory(((Player) inventoryClickEvent.getWhoClicked()));
    }
}
