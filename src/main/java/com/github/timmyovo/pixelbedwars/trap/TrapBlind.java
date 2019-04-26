package com.github.timmyovo.pixelbedwars.trap;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TrapBlind extends Trap {
    @Override
    public void executeTrap(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5, 1));
    }

    @Override
    public void tickTrap() {
        BedwarsGame bedwarsGame = PixelBedwars.getPixelBedwars()
                .getBedwarsGame();
        bedwarsGame.getGamePlayers()
                .stream()
                .filter(gamePlayer -> !gamePlayer.isTotallyDeath())
                .filter(gamePlayer -> {
                    GameTeam playerTeam = gamePlayer.getPlayerTeam();
                    if (playerTeam.getTeamShoppingProperties().isBlindTrap()) {
                        return gamePlayer.getPlayer().getLocation().distance(playerTeam.getTeamMeta().getTeamBedLocation().toBukkitLocation()) <= 8;
                    }
                    return false;
                })
                .forEach(gamePlayer -> {
                    executeTrap(gamePlayer.getPlayer());
                });
    }
}
