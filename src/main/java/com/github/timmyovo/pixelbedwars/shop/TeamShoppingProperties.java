package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.shop.item.ShopTeamItem;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamShoppingProperties {
    private static Map<GameTeam, List<ShopTeamItem>> teamShoppingProperties = new HashMap<>();

    public static List<ShopTeamItem> getTeamShopTeamItems(GameTeam gameTeam) {
        return teamShoppingProperties.getOrDefault(gameTeam, Lists.newArrayList());
    }

    public static boolean hasTeamBuyItem(GameTeam gameTeam, ShopTeamItem shopTeamItem) {
        return getTeamShopTeamItems(gameTeam).stream()
                .anyMatch(shopTeamItem::equals);
    }

    public static void notifyTeamBuyItem(GameTeam gameTeam, ShopTeamItem shopTeamItem) {
        getTeamShopTeamItems(gameTeam).add(shopTeamItem);
    }
}
