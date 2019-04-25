package com.github.timmyovo.pixelbedwars.shop;

import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.shop.config.EnchantmentEntry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TeamShoppingProperties {
    public EnchantmentEntry helmetEnchantment;
    public EnchantmentEntry chestplateEnchantment;
    public EnchantmentEntry leggingsEnchantment;
    public EnchantmentEntry bootsEnchantment;
    public EnchantmentEntry swordEnchantment;
    public boolean healthRegenEnable;
    public boolean doubleDragonEnable;
    private Map<Integer, Integer> iconLevelMap;
    private List<PotionEffect> potionList;

    public TeamShoppingProperties() {
        this.iconLevelMap = new HashMap<>();
        this.potionList = new ArrayList<>();
    }

    public Integer getTeamItemLevelById(int i) {//0 没买
        return iconLevelMap.getOrDefault(i, 0);
    }

    public void addTeamItemLevel(int i) {
        setTeamItemLevel(i, getTeamItemLevelById(i) + 1);
    }

    public void setTeamItemLevel(int i, int s) {
        this.iconLevelMap.put(i, s);
    }

    public void notifyTeamEquipmentChange(GameTeam playerTeam) {
        playerTeam.getAlivePlayers().forEach(gamePlayer -> {
            PlayerInventory inventory = gamePlayer.getPlayer().getInventory();
            if (helmetEnchantment != null) {
                inventory.getHelmet().getEnchantments()
                        .entrySet()
                        .stream()
                        .map(Map.Entry::getKey)
                        .forEach(enchantment -> inventory.getHelmet().removeEnchantment(enchantment));
                inventory.getHelmet().addUnsafeEnchantment(Enchantment.getByName(helmetEnchantment.getName()), helmetEnchantment.getLevel());
            }
            if (chestplateEnchantment != null) {
                inventory.getChestplate().getEnchantments()
                        .entrySet()
                        .stream()
                        .map(Map.Entry::getKey)
                        .forEach(enchantment -> inventory.getChestplate().removeEnchantment(enchantment));
                inventory.getChestplate().addUnsafeEnchantment(Enchantment.getByName(chestplateEnchantment.getName()), chestplateEnchantment.getLevel());
            }
            if (leggingsEnchantment != null) {
                inventory.getLeggings().getEnchantments()
                        .entrySet()
                        .stream()
                        .map(Map.Entry::getKey)
                        .forEach(enchantment -> inventory.getLeggings().removeEnchantment(enchantment));
                inventory.getLeggings().addUnsafeEnchantment(Enchantment.getByName(leggingsEnchantment.getName()), leggingsEnchantment.getLevel());
            }
            if (bootsEnchantment != null) {
                inventory.getBoots().getEnchantments()
                        .entrySet()
                        .stream()
                        .map(Map.Entry::getKey)
                        .forEach(enchantment -> inventory.getBoots().removeEnchantment(enchantment));
                inventory.getBoots().addUnsafeEnchantment(Enchantment.getByName(bootsEnchantment.getName()), bootsEnchantment.getLevel());
            }
            if (swordEnchantment != null) {
                for (ItemStack content : inventory.getContents()) {
                    if (content != null) {
                        if (isSword(content)) {
                            content.addUnsafeEnchantment(Enchantment.getByName(swordEnchantment.getName()), swordEnchantment.getLevel());
                        }
                    }
                }
            }
            this.potionList.forEach(potionEffect -> {
                gamePlayer.getPlayer().addPotionEffect(potionEffect);
            });
        });
    }

    public static boolean isSword(ItemStack itemStack) {
        return itemStack.getType().name().contains("SWORD") || itemStack.getType().name().contains("AXE");
    }
}
