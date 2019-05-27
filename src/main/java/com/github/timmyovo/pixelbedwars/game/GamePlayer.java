package com.github.timmyovo.pixelbedwars.game;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GamePlayer {
    private Player player;
    private int kills;
    private int death;
    private int finalKills;
    private int bedDestroyed;
    private boolean respawning;
    private boolean teamChat;
    private boolean totallyDeath;

    private ItemStack pickaxeItem;
    private ItemStack axeItem;

    public GamePlayer(Player player) {
        this.player = player;
        this.kills = 0;
        this.death = 0;
        this.finalKills = 0;
        this.bedDestroyed = 0;
        this.respawning = false;
        this.totallyDeath = false;
        this.teamChat = true;
        this.pickaxeItem = null;
        this.axeItem = null;
    }

    public void givePickaxe() {
        if (pickaxeItem == null) {
            return;
        }
        if (!player.getInventory().all(Material.WOOD_PICKAXE).isEmpty() || !player.getInventory().all(Material.IRON_PICKAXE).isEmpty() || !player.getInventory().all(Material.GOLD_PICKAXE).isEmpty() || !player.getInventory().all(Material.DIAMOND_PICKAXE).isEmpty()) {
            return;
        }
        this.player.getInventory().addItem(pickaxeItem);
    }

    public void giveAxe() {
        if (axeItem == null) {
            return;
        }
        if (!player.getInventory().all(Material.WOOD_AXE).isEmpty() || !player.getInventory().all(Material.IRON_AXE).isEmpty() || !player.getInventory().all(Material.GOLD_AXE).isEmpty() || !player.getInventory().all(Material.DIAMOND_AXE).isEmpty()) {
            return;
        }
        this.player.getInventory().addItem(axeItem);
    }

    public boolean canUpGradePickaxe() {
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_PICKAXE:
                return false;
            case GOLD_PICKAXE:
            case IRON_PICKAXE:
            case WOOD_PICKAXE:
        }
        return true;
    }

    public boolean canUpGradeAxe() {
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_AXE:
                return false;
            case GOLD_AXE:
            case IRON_AXE:
            case WOOD_AXE:
        }
        return true;
    }

    public boolean upGradePickaxe() {
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_PICKAXE:
                return false;
            case GOLD_PICKAXE:
                this.pickaxeItem.setType(Material.DIAMOND_PICKAXE);
                break;
            case IRON_PICKAXE:
                this.pickaxeItem.setType(Material.GOLD_PICKAXE);
                break;
            case WOOD_PICKAXE:
                this.pickaxeItem.setType(Material.IRON_PICKAXE);
                break;
        }
        return true;
    }

    public boolean upGradeAxe() {
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_AXE:
                return false;
            case GOLD_AXE:
                this.pickaxeItem.setType(Material.DIAMOND_AXE);
                break;
            case IRON_AXE:
                this.pickaxeItem.setType(Material.GOLD_AXE);
                break;
            case WOOD_AXE:
                this.pickaxeItem.setType(Material.IRON_AXE);
                break;
        }
        return true;
    }


    public void downGradePickaxe() {
        if (pickaxeItem == null) {
            return;
        }
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_PICKAXE:
                this.pickaxeItem.setType(Material.GOLD_PICKAXE);
                break;
            case GOLD_PICKAXE:
                this.pickaxeItem.setType(Material.IRON_PICKAXE);
                break;
            case IRON_PICKAXE:
                this.pickaxeItem.setType(Material.WOOD_PICKAXE);
                break;
            case WOOD_PICKAXE:
                break;
        }
    }

    public void downGradeAxe() {
        if (axeItem == null) {
            return;
        }
        switch (this.pickaxeItem.getType()) {
            case DIAMOND_AXE:
                this.pickaxeItem.setType(Material.GOLD_AXE);
                break;
            case GOLD_AXE:
                this.pickaxeItem.setType(Material.IRON_AXE);
                break;
            case IRON_AXE:
                this.pickaxeItem.setType(Material.WOOD_AXE);
                break;
            case WOOD_AXE:
                break;
        }
    }

    public void addDeath() {
        this.death++;
    }

    public void addKill() {
        this.kills++;
    }

    public boolean isPlayerEqual(Player player) {
        Validate.notNull(player);
        return this.player.getUniqueId().equals(player.getUniqueId());
    }

    public GameTeam getPlayerTeam() {
        return PixelBedwars.getPixelBedwars().getBedwarsGame().getPlayerTeam(this);
    }

    public void addFinalKills() {
        this.finalKills++;
    }

    public void addBedDestroyed() {
        this.bedDestroyed++;
    }
}
