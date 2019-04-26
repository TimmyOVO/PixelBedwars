package com.github.timmyovo.pixelbedwars.trap;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TrapDigSpeed extends Trap {
    @Override
    public void executeTrap(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10, 1));
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
                    return bedwarsGame.getTeamList().stream()
                            .anyMatch(gameTeam -> {
                                if (gameTeam != playerTeam) {
                                    if (gameTeam.getTeamShoppingProperties().isBlindTrap()) {
                                        return gamePlayer.getPlayer().getLocation().distance(gameTeam.getTeamMeta().getTeamBedLocation().toBukkitLocation()) <= 8;
                                    }
                                }
                                return true;
                            });
                })
                .forEach(gamePlayer -> {
                    gamePlayer.getPlayerTeam().getTeamShoppingProperties().setBlindTrap(false);
                    executeTrap(gamePlayer.getPlayer());
                });
    }
}
