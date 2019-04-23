package com.github.timmyovo.pixelbedwars.shop.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnchantmentEntry {
    private String name;
    private int level;

    public void applyEnchantment(ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(Enchantment.getByName(name), level);
    }
}
