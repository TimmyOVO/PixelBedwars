package com.github.timmyovo.pixelbedwars.settings.item;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RandomInventoryItem {
    private InventoryItem inventoryItem;
}
