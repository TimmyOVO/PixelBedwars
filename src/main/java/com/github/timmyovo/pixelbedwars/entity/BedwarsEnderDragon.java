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
import java.util.stream.Collectors;

public class BedwarsEnderDragon extends EntityEnderDragon {
    private BedwarsGame bedwarsGame;
    private GameTeam gameTeam;
    private int idleTicks;

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
        if (idleTicks >= 200) {
            if (!(target instanceof Player)) {
                List<GameTeam> collect = bedwarsGame.getTeamList()
                        .stream()
                        .filter(gameTeam1 -> !gameTeam1.equals(gameTeam))
                        .sorted()
                        .collect(Collectors.toList());
                Collections.shuffle(collect);
                if (collect.isEmpty()) {
                    target = null;
                } else {
                    List<GamePlayer> alivePlayers = new ArrayList<>(collect.get(0).getAlivePlayers());
                    if (alivePlayers.isEmpty()) {
                        target = null;
                    } else {
                        Collections.shuffle(alivePlayers);
                        target = ((CraftPlayer) alivePlayers.get(0).getPlayer()).getHandle();
                    }

                }
            }
            if (target != null) {
                //找到目标
                idleTicks = 0;
            }
        }
        this.idleTicks++;
        super.m();
    }

    @Override
    protected void aZ() {

    }
}
