package com.github.timmyovo.pixelbedwars.items;

import net.minecraft.server.v1_8_R3.EntityProjectile;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftProjectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class BridgeEgg {
    public void onEntityMove(ProjectileLaunchEvent projectileLaunchEvent) {
        EntityProjectile handle = ((CraftProjectile) projectileLaunchEvent.getEntity()).getHandle();

    }
}
