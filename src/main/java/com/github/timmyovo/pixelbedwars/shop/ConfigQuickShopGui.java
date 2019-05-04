package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.database.PlayerQuickShopEntryModel;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class ConfigQuickShopGui extends ShopGui {
    public static final String CONFIG_QUICK_SHOP_KEY = "CONFIG_QUICK_SHOP_KEY";

    public ConfigQuickShopGui(PlayerShop shop) {
        super(shop);
        this.inventory = Bukkit.createInventory(null, 54, bedwarsGame.getLanguage().getQuickShopConfigGuiName());
    }

    @Override
    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent inventoryClickEvent) {
        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory.getName().equals(PixelBedwars.getPixelBedwars().getLanguage().getQuickShopConfigGuiName())) {
            inventoryClickEvent.setCancelled(true);
            notifyClick(inventoryClickEvent);
        }
    }

    public void setPlayerShopItem(Player player, ShopItem shopItem) {
        if (shopItem == null) {
            player.removeMetadata(CONFIG_QUICK_SHOP_KEY, PixelBedwars.getPixelBedwars());
            return;
        }
        player.setMetadata(CONFIG_QUICK_SHOP_KEY, new FixedMetadataValue(PixelBedwars.getPixelBedwars(), shopItem.toString()));
    }

    @Override
    protected void notifyClick(InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getSlot() <= 17) {
            return;
        }
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        if (!player.hasMetadata(CONFIG_QUICK_SHOP_KEY)) {
            return;
        }
        List<MetadataValue> metadata = player.getMetadata(CONFIG_QUICK_SHOP_KEY);
        if (metadata.isEmpty()) {
            return;
        }
        ShopItem shopItem = ShopItem.fromString(metadata.get(0).asString());
        if (shopItem == null) {
            return;
        }
        PlayerQuickShopEntryModel.setPlayerQuickShopEntry(inventoryClickEvent.getWhoClicked().getUniqueId(), inventoryClickEvent.getSlot(), shopItem.toString());
        setPlayerShopItem(player, null);
        PixelBedwars.getPixelBedwars().getPlayerShopGui().show(player);
        player.sendMessage("保存成功");

    }

    @Override
    public void onPlayerInteract(PlayerInteractEntityEvent playerInteractEntityEvent) {

    }
}
