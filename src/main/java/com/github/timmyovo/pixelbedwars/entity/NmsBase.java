package com.github.timmyovo.pixelbedwars.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;

public abstract class NmsBase {

    public static final EntityType ENTITY = EntityType.COW;

    protected HashMap<LivingEntity, Corpses.CorpseData> allSlimes = new HashMap<>();

    private void openInventory(Player player, Corpses.CorpseData cd) {
        try {
            InventoryView view = player.openInventory(cd.getLootInventory());
            cd.setInventoryView(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateCows() {
    }

    private Location moveAmount(Location l, int rotation) {
        l = l.clone();
        if (rotation == 0) {
            l = l.add(0, -0.8, -0.9);
            l.setYaw(0);
        } else if (rotation == 1) {
            l = l.add(+0.9, -0.8, 0);
            l.setYaw(90);
        } else if (rotation == 2) {
            l = l.add(0, -0.8, +0.9);
            l.setYaw(180);
        } else if (rotation == 3) {
            l = l.add(-0.9, -0.8, 0);
            l.setYaw(270);
        }
        return l;
    }

    protected abstract void addNbtTagsToSlime(LivingEntity slime);

    public Location getNonClippableBlockUnderPlayer(Location loc, int addToYPos) {
        if (loc.getBlockY() < 0) {
            return null;
        }
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Block block = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            Material m = block.getType();
            if (m.isSolid()) {
                float slabAdjust = 0.0F;
                if (isLowerSlab(block)) {
                    slabAdjust = -0.5F;
                }
                return new Location(loc.getWorld(), loc.getX(), y + addToYPos + slabAdjust, loc.getZ());
            }
        }

        return new Location(loc.getWorld(), loc.getX(), 1 + addToYPos, loc.getZ());
    }

    @SuppressWarnings("deprecation")
    private boolean isLowerSlab(Block block) {
        int id = block.getType().getId();
        if (id == 44 || id == 126 || id == 182 || id == 205) {
            int data = block.getData();
            if (data < 8) {
                return true;
            }
        }

        return false;
    }


    public boolean isInViewDistance(Player p, Corpses.CorpseData data) {
        Location p1loc = p.getLocation();
        Location p2loc = data.getTrueLocation();
        double minX = p2loc.getX() - 45;
        double minY = p2loc.getY() - 45;
        double minZ = p2loc.getZ() - 45;
        double maxX = p2loc.getX() + 45;
        double maxY = p2loc.getY() + 45;
        double maxZ = p2loc.getZ() + 45;
        return p1loc.getX() >= minX && p1loc.getX() <= maxX
                && p1loc.getY() >= minY && p1loc.getY() <= maxY
                && p1loc.getZ() >= minZ && p1loc.getZ() <= maxZ;
    }
}
