package com.github.timmyovo.pixelbedwars.shop;

import com.github.skystardust.ultracore.bukkit.commands.MainCommandSpec;
import com.github.skystardust.ultracore.bukkit.commands.SubCommandSpec;
import com.github.timmyovo.pixelbedwars.shop.category.ShopCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerShop {
    private EntityType entityType;
    private String displayName;
    private Map<Integer, ShopCategory> categoryItems;

    public static void initShopCommands() {
        MainCommandSpec.newBuilder()
                .addAlias("pbs")
                .addAlias("pixelbedwarsshop")
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("")
                        .build())
                .withCommandSpecExecutor((commandSender, strings) -> {
                    return true;
                })
                .build()
                .register();
    }
}
