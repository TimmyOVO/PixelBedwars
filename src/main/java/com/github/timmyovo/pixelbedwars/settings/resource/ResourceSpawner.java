package com.github.timmyovo.pixelbedwars.settings.resource;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSpawner {
    private int spawnInterval;
    private VecLoc3D spawnerLocation;
    private InventoryItem itemToSpawn;
    private Material hologramIcon;
    private List<String> hologramTexts;
    private transient int remainTicks;
    private transient int multiplier;

    public ResourceSpawner(int spawnInterval, VecLoc3D spawnerLocation, SpawnerType spawnerType) {
        this.spawnInterval = spawnInterval;
        this.spawnerLocation = spawnerLocation;
        this.multiplier = 1;
        switch (spawnerType) {
            case IRON:
                this.itemToSpawn = InventoryItem.builder()
                        .itemstackData(new ItemStack(org.bukkit.Material.IRON_INGOT).serialize())
                        .build();
                break;
            case GOLD:
                this.itemToSpawn = InventoryItem.builder()
                        .itemstackData(new ItemStack(org.bukkit.Material.GOLD_INGOT).serialize())
                        .build();
                break;
            case DIAMOND:
                this.itemToSpawn = InventoryItem.builder()
                        .itemstackData(new ItemStack(org.bukkit.Material.DIAMOND).serialize())
                        .build();
                this.hologramIcon = Material.DIAMOND_BLOCK;
                break;
            case EMERALD:
                this.itemToSpawn = InventoryItem.builder()
                        .itemstackData(new ItemStack(Material.EMERALD).serialize())
                        .build();
                this.hologramIcon = Material.EMERALD_BLOCK;
                break;
        }
    }

    public void tickInterval() {
        if (remainTicks == 0) {
            remainTicks = (spawnInterval * 20) / multiplier;
            Location location = spawnerLocation.toBukkitLocation();
            location.getWorld().dropItem(location, itemToSpawn.toItemStack());
            return;
        }
        remainTicks -= 10;
    }

    public static enum SpawnerType {
        IRON, GOLD, DIAMOND, EMERALD
    }
}
