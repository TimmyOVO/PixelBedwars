package com.github.timmyovo.pixelbedwars.trap;

import org.bukkit.entity.Player;

public abstract class Trap {

    public abstract void executeTrap(Player player);

    public abstract void tickTrap();
}
