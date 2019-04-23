package com.github.timmyovo.pixelbedwars.shop.item;

import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class ShopTeamItem extends ShopItem {
    private String command;

    @Override
    public void requestBuyItem(GamePlayer gamePlayer) {
        String temp = command;
        temp = ChatColor.translateAlternateColorCodes('&', temp);
        temp = PlaceholderAPI.setPlaceholders(gamePlayer.getPlayer(), temp);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp);
    }
}
