package com.github.timmyovo.pixelbedwars.shop.item;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class ShopTeamItem extends ShopItem {
    private String command;

    private ShopTeamItem(Builder builder) {
        setIcon(builder.icon);
        setItems(builder.items);
        setRequireItem(builder.requireItem);
        setRequirePermission(builder.requirePermission);
        setCommand(builder.command);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void requestBuyItem(GamePlayer gamePlayer) {
        String temp = command;
        temp = ChatColor.translateAlternateColorCodes('&', temp);
        temp = PlaceholderAPI.setPlaceholders(gamePlayer.getPlayer(), temp);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), temp);
    }

    public static ShopTeamItem fromString(String string) {
        return FileUtils.GSON.fromJson(string, ShopTeamItem.class);
    }

    @Override
    public String toString() {
        return FileUtils.GSON.toJson(this);
    }

    public static final class Builder {
        private InventoryItem icon;
        private List<InventoryItem> items;
        private InventoryItem requireItem;
        private String requirePermission;
        private String command;

        private Builder() {
        }

        public Builder setIcon(InventoryItem val) {
            icon = val;
            return this;
        }

        public Builder setItems(List<InventoryItem> val) {
            items = val;
            return this;
        }

        public Builder setRequireItem(InventoryItem val) {
            requireItem = val;
            return this;
        }

        public Builder setRequirePermission(String val) {
            requirePermission = val;
            return this;
        }

        public Builder setCommand(String val) {
            command = val;
            return this;
        }

        public ShopTeamItem build() {
            return new ShopTeamItem(this);
        }
    }
}
