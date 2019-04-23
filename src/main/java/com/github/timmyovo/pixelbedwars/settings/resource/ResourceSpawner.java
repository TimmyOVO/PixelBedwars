package com.github.timmyovo.pixelbedwars.settings.resource;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTextLine;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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
    private transient Hologram hologram;
    private transient ArmorStand entityArmorStand;
    private transient BukkitTask updateArmorStandTask;

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
                this.hologramTexts = Lists.newArrayList();
                break;
            case EMERALD:
                this.itemToSpawn = InventoryItem.builder()
                        .itemstackData(new ItemStack(Material.EMERALD).serialize())
                        .build();
                this.hologramIcon = Material.EMERALD_BLOCK;
                this.hologramTexts = Lists.newArrayList();
                break;
        }
    }

    public void tickInterval() {
        Location source = spawnerLocation.toBukkitLocation();

        if (updateArmorStandTask == null) {
            updateArmorStandTask = Bukkit.getScheduler().runTaskTimer(PixelBedwars.getPixelBedwars(), () -> {
                if (entityArmorStand == null) {
                    this.entityArmorStand = (ArmorStand) source.getWorld().spawnEntity(source.clone().add(0, 2, 0), EntityType.ARMOR_STAND);
                    this.entityArmorStand.setVisible(false);
                    this.entityArmorStand.setHelmet(new ItemStack(hologramIcon));
                    this.entityArmorStand.setGravity(false);
                } else {
                    Location location = entityArmorStand.getLocation();
                    location.setYaw(location.getYaw() + 5);
                    this.entityArmorStand.teleport(location);
                }
            }, 0L, 1L);
        }
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(PixelBedwars.getPixelBedwars(), source.clone().add(0, 4, 0));
        }
        if (getHologramTexts() != null) {
            for (int i = 0; i < getHologramTexts().size(); i++) {
                String text = getHologramTexts().get(i);
                text = ChatColor.translateAlternateColorCodes('&', text);
                text = text.replace("%interval%", String.valueOf(remainTicks / 20));
                try {
                    HologramLine line = hologram.getLine(i);
                    if (line instanceof CraftTextLine) {
                        if (!((CraftTextLine) line).getText().equals(text)) {
                            ((CraftTextLine) line).setText(text);
                        }
                    }
                } catch (Exception e) {
                    hologram.appendTextLine(text);
                }
            }
        }
        if (multiplier == 0) {
            multiplier = 1;
        }
        if (remainTicks == 0) {
            remainTicks = (spawnInterval * 20);
            source.getWorld().dropItem(source, itemToSpawn.toItemStack());
            return;
        }
        remainTicks -= 10 * multiplier;
    }

    public static enum SpawnerType {
        IRON, GOLD, DIAMOND, EMERALD
    }
}
