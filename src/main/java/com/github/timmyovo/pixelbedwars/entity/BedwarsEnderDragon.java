package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BedwarsEnderDragon extends EntityEnderDragon {
    private BedwarsGame bedwarsGame;
    private GameTeam gameTeam;

    public BedwarsEnderDragon(World world, GameTeam gameTeam) {
        super(world);
        this.bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
        this.gameTeam = gameTeam;
        setCustomName(gameTeam.getTeamMeta().getFormatTeamName() + "的龙");
        setCustomNameVisible(true);
    }

    public void spawnEntity(Location location) {
        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.world.addEntity(this);
    }

    @Override
    public void m() {
        if (!(target instanceof Player)) {
            List<GamePlayer> alivePlayers = new ArrayList<>(bedwarsGame.getTeamList()
                    .stream()
                    .filter(gameTeam1 -> !gameTeam1.equals(gameTeam))
                    .sorted()
                    .findAny()
                    .orElse(bedwarsGame.getTeamList().get(0))
                    .getAlivePlayers());
            Collections.shuffle(alivePlayers);
            target = ((CraftPlayer) alivePlayers.get(0).getPlayer()).getHandle();
        }
        super.m();
    }
}
