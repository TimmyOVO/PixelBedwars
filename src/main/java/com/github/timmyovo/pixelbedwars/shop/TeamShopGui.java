package com.github.timmyovo.pixelbedwars.shop;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.settings.resource.ResourceSpawner;
import com.github.timmyovo.pixelbedwars.shop.config.EnchantmentEntry;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import com.github.timmyovo.pixelbedwars.trap.TrapGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/*
 * 5
 * 10
 * 20
 * 30
 * 保护
 * 4
 * 6
 * 急迫
 * 4
 * 8
 * 10
 * 12
 * 16
 * 资源点升级
 * 3
 * 基地回血
 * 5
 * 末影龙增益
 */

/*
 * 保护
 */

public class TeamShopGui implements Listener {
    private BedwarsGame bedwarsGame;
    private Inventory inventory;

    public TeamShopGui() {
        this.bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
        this.inventory = Bukkit.createInventory(null, 54, bedwarsGame.getLanguage().getTeamShopDisplayName());
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i + 18, new ItemStack(Material.STAINED_GLASS_PANE));
        }
    }

    private void applyItems(Player player) {
        GameTeam playerTeam = bedwarsGame.getPlayerTeam(player);
        this.inventory.setItem(10, getEnchantmentSharpnessIcon(player, playerTeam));
        this.inventory.setItem(11, getEnchantmentProtectionIcon(player, playerTeam));
        this.inventory.setItem(12, getDigSpeedUpgradeIcon(player, playerTeam));
        this.inventory.setItem(13, getResourceMultiplierIcon(player, playerTeam));
        this.inventory.setItem(14, getBaseHealthRegenIcon(player, playerTeam));
        this.inventory.setItem(15, getDoubleDragonIcon(player, playerTeam));
        this.inventory.setItem(16, getTrapIcon(player));
    }

    private ItemStack getEnchantmentSharpnessIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(1);//附魔锋利
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.IRON_SWORD));
        itemFactory.setDisplayName("§c锋利附魔");
        itemFactory.addLore(" ");
        itemFactory.addLore("§7为你方所有人员的剑和斧添加锋利附魔Ⅰ!");
        itemFactory.addLore(" ");
        if (level == 1) {
            itemFactory.addLore("§c已经购买!");
            return itemFactory.pack();
        }
        if (level == 0) {
            if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 8))) {
                itemFactory.addLore("§a购买可用");
            } else {
                itemFactory.addLore("§c没有足够的物品");
            }
        }
        return itemFactory.pack();
    }

    private ItemStack getEnchantmentProtectionIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(2);//保护附魔
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.IRON_CHESTPLATE));
        itemFactory.setDisplayName("§c团队保护");
        itemFactory.addLore(" ");
        itemFactory.addLore("§7为你方所有人员的护腿和靴子添加保护Ⅰ! §b5钻石");
        itemFactory.addLore("§7为你方所有人员的护腿和靴子添加保护Ⅱ! §b10钻石");
        itemFactory.addLore("§7为你方所有人员的护腿和靴子添加保护Ⅲ! §b20钻石");
        itemFactory.addLore("§7为你方所有人员的护腿和靴子添加保护Ⅳ! §b30钻石");

        switch (level) {
            case 0:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 5))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                break;
            case 1:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 10))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a为你方所有人员的护腿和靴子添加保护Ⅰ! §b5钻石");
                break;
            case 2:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 20))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a为你方所有人员的护腿和靴子添加保护Ⅰ! §b5钻石");
                setLore(itemFactory, 2, "§a为你方所有人员的护腿和靴子添加保护Ⅱ! §b10钻石");
                break;
            case 3:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 30))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a为你方所有人员的护腿和靴子添加保护Ⅰ! §b5钻石");
                setLore(itemFactory, 2, "§a为你方所有人员的护腿和靴子添加保护Ⅱ! §b10钻石");
                setLore(itemFactory, 3, "§a为你方所有人员的护腿和靴子添加保护Ⅲ! §b20钻石");
                break;
            case 4:
                itemFactory.addLore("§a已全部购买");
                setLore(itemFactory, 1, "§a为你方所有人员的护腿和靴子添加保护Ⅰ! §b5钻石");
                setLore(itemFactory, 2, "§a为你方所有人员的护腿和靴子添加保护Ⅱ! §b10钻石");
                setLore(itemFactory, 3, "§a为你方所有人员的护腿和靴子添加保护Ⅲ! §b20钻石");
                setLore(itemFactory, 4, "§a为你方所有人员的护腿和靴子添加保护Ⅳ! §b30钻石");
                break;
        }
        return itemFactory.pack();
    }

    private ItemStack getDigSpeedUpgradeIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(3);//挖掘药水
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.GOLD_PICKAXE));
        itemFactory.setDisplayName("§c急速矿工");
        itemFactory.addLore(" ");
        itemFactory.addLore("§7为你方所有人员添加急迫Ⅰ! §b4钻石");
        itemFactory.addLore("§7为你方所有人员添加急迫Ⅱ! §b6钻石");

        switch (level) {
            case 0:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 4))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                break;
            case 1:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 6))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a为你方所有人员添加急迫Ⅰ! §b4钻石");
                break;
            case 2:
                itemFactory.addLore("§a已全部购买");
                setLore(itemFactory, 1, "§a为你方所有人员添加急迫Ⅰ! §b4钻石");
                setLore(itemFactory, 2, "§a为你方所有人员添加急迫Ⅱ! §b6钻石");
                break;
        }
        return itemFactory.pack();
    }

    private ItemStack getResourceMultiplierIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(4);//资源刷新率
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.FURNACE));
        itemFactory.setDisplayName("§c熔炉升级");
        itemFactory.addLore(" ");
        itemFactory.addLore("§71阶:+50%资源! §b4钻石");
        itemFactory.addLore("§72阶:+100%资源! §b8钻石");
        itemFactory.addLore("§73阶:生成绿宝石! §b12钻石");
        itemFactory.addLore("§74阶:+200%资源! §b16钻石");

        switch (level) {
            case 0:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 4))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                break;
            case 1:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 8))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a1阶:+50%资源! §b4钻石");
                break;
            case 2:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 12))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a1阶:+50%资源! §b4钻石");
                setLore(itemFactory, 2, "§a2阶:+100%资源! §b8钻石");
                break;
            case 3:
                if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 16))) {
                    itemFactory.addLore("§a购买可用");
                } else {
                    itemFactory.addLore("§c没有足够的物品");
                }
                setLore(itemFactory, 1, "§a1阶:+50%资源! §b4钻石");
                setLore(itemFactory, 2, "§a2阶:+100%资源! §b8钻石");
                setLore(itemFactory, 3, "§a3阶:生成绿宝石! §b12钻石");
                break;
            case 4:
                itemFactory.addLore("§a已全部购买");
                setLore(itemFactory, 1, "§a1阶:+50%资源! §b4钻石");
                setLore(itemFactory, 2, "§a2阶:+100%资源! §b8钻石");
                setLore(itemFactory, 3, "§a3阶:生成绿宝石! §b12钻石");
                setLore(itemFactory, 4, "§a4阶:+200%资源! §b16钻石");
                break;
        }
        return itemFactory.pack();
    }

    private ItemStack getTrapIcon(Player player) {
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.LEATHER));
        itemFactory.setDisplayName("§e陷阱");
        return itemFactory.pack();
    }

    private ItemStack getBaseHealthRegenIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(5);//基地生命恢复
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.BEACON));
        itemFactory.setDisplayName("§c生命之泉");
        itemFactory.addLore(" ");
        itemFactory.addLore("§7为你方所有在基地附近的人员添加生命恢复效果!");
        itemFactory.addLore(" ");
        if (level == 1) {
            itemFactory.addLore("§c已经购买!");
            return itemFactory.pack();
        }
        if (level == 0) {
            if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 3))) {
                itemFactory.addLore("§a购买可用");
            } else {
                itemFactory.addLore("§c没有足够的物品");
            }
        }
        return itemFactory.pack();
    }

    private ItemStack getDoubleDragonIcon(Player player, GameTeam gameTeam) {
        TeamShoppingProperties teamShoppingProperties = gameTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(6);//双倍末影龙
        ItemFactory itemFactory = new ItemFactory(() -> new ItemStack(Material.DRAGON_EGG));
        itemFactory.setDisplayName("§c生命之泉");
        itemFactory.addLore(" ");
        itemFactory.addLore("§7为你方所有在基地附近的人员添加生命恢复效果!");
        itemFactory.addLore(" ");
        if (level == 1) {
            itemFactory.addLore("§c已经购买!");
            return itemFactory.pack();
        }
        if (level == 0) {
            if (ShopItem.hasEnoughItems(player, new ItemStack(Material.DIAMOND, 3))) {
                itemFactory.addLore("§a购买可用");
            } else {
                itemFactory.addLore("§c没有足够的物品");
            }
        }
        return itemFactory.pack();
    }

    public void setLore(ItemFactory factory, int index, String lore) {
        List<String> lore1 = factory.getItemMeta().getLore();
        lore1.set(index, lore);
        factory.setLore(lore1);
    }

    public void show(Player gamePlayer) {
        applyItems(gamePlayer);
        gamePlayer.openInventory(inventory);
    }

    private void notifyClick(InventoryClickEvent inventoryClickEvent) {

        ItemStack currentItem = inventoryClickEvent.getCurrentItem();
        if (currentItem == null) {
            return;
        }
        buyItem(inventoryClickEvent);
    }

    private void buyItem(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        GameTeam playerTeam = bedwarsGame.getPlayerTeam(player);
        int slot = inventoryClickEvent.getSlot();
        switch (slot) {
            case 10:
                requestBuyEnchantmentSharpness(player, playerTeam);
                break;
            case 11:
                requestBuyEnchantmentProtection(player, playerTeam);
                break;
            case 12:
                requestBuyDigSpeedUpgrade(player, playerTeam);
                break;
            case 13:
                requestBuyResourceMultiplier(player, playerTeam);
                break;
            case 14:
                requestBuyBaseHealthRegen(player, playerTeam);
                break;
            case 15:
                requestBuyDoubleDragon(player, playerTeam);
                break;
            case 16:
                new TrapGui().show(player);
                break;
        }
        applyItems(player);
    }

    private void requestBuyDoubleDragon(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(6);
        if (level != 0) {
            player.sendMessage("你已经购买过了");
            return;
        }
        ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 5);
        if (ShopItem.hasEnoughItems(player, inventoryItem)) {
            ShopItem.takeItem(player, inventoryItem);
            teamShoppingProperties.setTeamItemLevel(6, 1);
            teamShoppingProperties.doubleDragonEnable = true;
            player.sendMessage("购买额外末影龙成功");
        } else {
            player.sendMessage("你没有足够的物品!");
        }
    }

    private void requestBuyBaseHealthRegen(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(5);
        if (level != 0) {
            player.sendMessage("你已经购买过了");
            return;
        }
        ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 3);
        if (ShopItem.hasEnoughItems(player, inventoryItem)) {
            ShopItem.takeItem(player, inventoryItem);
            teamShoppingProperties.setTeamItemLevel(5, 1);
            teamShoppingProperties.healthRegenEnable = true;
            player.sendMessage("购买血量恢复成功");
        } else {
            player.sendMessage("你没有足够的物品!");
        }
    }

    private void requestBuyResourceMultiplier(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(4);
        if (level + 1 > 4) {
            player.sendMessage("你已经购买过了");
            return;
        }
        switch (level) {
            case 0://1
                ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 4);
                if (ShopItem.hasEnoughItems(player, inventoryItem)) {
                    ShopItem.takeItem(player, inventoryItem);
                    playerTeam.getTeamMeta().allSpawnerMultiplier(2);
                    teamShoppingProperties.setTeamItemLevel(4, 1);
                    player.sendMessage("购买资源刷新1成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 1:
                ItemStack inventoryItem1 = new ItemStack(Material.DIAMOND, 8);
                if (ShopItem.hasEnoughItems(player, inventoryItem1)) {
                    ShopItem.takeItem(player, inventoryItem1);
                    teamShoppingProperties.setTeamItemLevel(4, 2);
                    playerTeam.getTeamMeta().allSpawnerMultiplier(2);
                    player.sendMessage("购买资源刷新2成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 2:
                ItemStack inventoryItem2 = new ItemStack(Material.DIAMOND, 12);
                if (ShopItem.hasEnoughItems(player, inventoryItem2)) {
                    ShopItem.takeItem(player, inventoryItem2);
                    teamShoppingProperties.setTeamItemLevel(4, 3);
                    VecLoc3D spawnerLocation = playerTeam.getTeamMeta().getIronSpawnerList().get(0).getSpawnerLocation();
                    playerTeam.getTeamMeta().getEmeraldSpawnerList().add(new ResourceSpawner(45, spawnerLocation, ResourceSpawner.SpawnerType.EMERALD));
                    player.sendMessage("购买资源刷新3成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 3:
                ItemStack inventoryItem3 = new ItemStack(Material.DIAMOND, 16);
                if (ShopItem.hasEnoughItems(player, inventoryItem3)) {
                    ShopItem.takeItem(player, inventoryItem3);
                    teamShoppingProperties.setTeamItemLevel(4, 4);
                    playerTeam.getTeamMeta().allSpawnerMultiplier(5);
                    player.sendMessage("购买资源刷新4成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
        }
    }

    private void requestBuyDigSpeedUpgrade(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(3);
        if (level + 1 > 2) {
            player.sendMessage("你已经购买过了");
            return;
        }

        switch (level) {
            case 0://1
                ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 4);
                if (ShopItem.hasEnoughItems(player, inventoryItem)) {
                    ShopItem.takeItem(player, inventoryItem);
                    PotionEffect potionEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 99999, 1, true);
                    teamShoppingProperties.setTeamItemLevel(3, 1);
                    teamShoppingProperties.getPotionList().add(potionEffect);
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买急迫1成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 1:
                ItemStack inventoryItem1 = new ItemStack(Material.DIAMOND, 6);
                if (ShopItem.hasEnoughItems(player, inventoryItem1)) {
                    ShopItem.takeItem(player, inventoryItem1);
                    PotionEffect potionEffect2 = new PotionEffect(PotionEffectType.FAST_DIGGING, 99999, 2, true);
                    teamShoppingProperties.getPotionList().add(potionEffect2);
                    teamShoppingProperties.setTeamItemLevel(3, 2);
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买急迫2成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
        }
    }

    private void requestBuyEnchantmentProtection(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(2);
        if (level + 1 > 4) {
            player.sendMessage("你已经购买过了");
            return;
        }
        EnchantmentEntry enchantmentEntry = EnchantmentEntry.builder()
                .level(1)
                .name(Enchantment.PROTECTION_ENVIRONMENTAL.getName())
                .build();
        switch (level) {
            case 0://1
                ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 5);
                if (ShopItem.hasEnoughItems(player, inventoryItem)) {
                    ShopItem.takeItem(player, inventoryItem);
                    enchantmentEntry.setLevel(1);
                    teamShoppingProperties.setTeamItemLevel(2, 1);
                    teamShoppingProperties.leggingsEnchantment = enchantmentEntry;
                    teamShoppingProperties.bootsEnchantment = enchantmentEntry;
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买保护1成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 1:
                ItemStack inventoryItem1 = new ItemStack(Material.DIAMOND, 10);
                if (ShopItem.hasEnoughItems(player, inventoryItem1)) {
                    ShopItem.takeItem(player, inventoryItem1);
                    enchantmentEntry.setLevel(2);
                    teamShoppingProperties.setTeamItemLevel(2, 2);
                    teamShoppingProperties.leggingsEnchantment = enchantmentEntry;
                    teamShoppingProperties.bootsEnchantment = enchantmentEntry;
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买保护2成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 2:
                ItemStack inventoryItem2 = new ItemStack(Material.DIAMOND, 20);
                if (ShopItem.hasEnoughItems(player, inventoryItem2)) {
                    ShopItem.takeItem(player, inventoryItem2);
                    enchantmentEntry.setLevel(3);
                    teamShoppingProperties.setTeamItemLevel(2, 3);
                    teamShoppingProperties.leggingsEnchantment = enchantmentEntry;
                    teamShoppingProperties.bootsEnchantment = enchantmentEntry;
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买保护3成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
            case 3:
                ItemStack inventoryItem3 = new ItemStack(Material.DIAMOND, 30);
                if (ShopItem.hasEnoughItems(player, inventoryItem3)) {
                    ShopItem.takeItem(player, inventoryItem3);
                    enchantmentEntry.setLevel(4);
                    teamShoppingProperties.setTeamItemLevel(2, 4);
                    teamShoppingProperties.leggingsEnchantment = enchantmentEntry;
                    teamShoppingProperties.bootsEnchantment = enchantmentEntry;
                    teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
                    player.sendMessage("购买保护4成功");
                } else {
                    player.sendMessage("你没有足够的物品!");
                }
                break;
        }
    }

    private void requestBuyEnchantmentSharpness(Player player, GameTeam playerTeam) {
        TeamShoppingProperties teamShoppingProperties = playerTeam.getTeamShoppingProperties();
        Integer level = teamShoppingProperties.getTeamItemLevelById(1);
        if (level != 0) {
            player.sendMessage("你已经购买过了");
            return;
        }
        ItemStack inventoryItem = new ItemStack(Material.DIAMOND, 8);
        if (ShopItem.hasEnoughItems(player, inventoryItem)) {
            ShopItem.takeItem(player, inventoryItem);
            teamShoppingProperties.setTeamItemLevel(1, level + 1);
            teamShoppingProperties.swordEnchantment = EnchantmentEntry.builder()
                    .level(1)
                    .name(Enchantment.DAMAGE_ALL.getName())
                    .build();
            teamShoppingProperties.notifyTeamEquipmentChange(playerTeam);
            player.sendMessage("购买锋利1成功");
            return;
        }
        player.sendMessage("你没有足够的资源购买这个物品");
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent inventoryClickEvent) {
        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        if (clickedInventory == inventoryClickEvent.getWhoClicked().getInventory()) {
            if (inventoryClickEvent.getSlot() <= 39 && inventoryClickEvent.getSlot() >= 36) {
                inventoryClickEvent.setCancelled(true);
            }
            return;
        }
        if (clickedInventory.getName().equals(PixelBedwars.getPixelBedwars().getLanguage().getTeamShopDisplayName())) {
            inventoryClickEvent.setCancelled(true);
            notifyClick(inventoryClickEvent);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent playerInteractEntityEvent) {
        Entity rightClicked = playerInteractEntityEvent.getRightClicked();
        if (rightClicked == null) {
            return;
        }
        if (rightClicked.getType() == bedwarsGame.getGameSetting().getTeamGuiEntityType()) {
            show(playerInteractEntityEvent.getPlayer());
        }
    }

}
