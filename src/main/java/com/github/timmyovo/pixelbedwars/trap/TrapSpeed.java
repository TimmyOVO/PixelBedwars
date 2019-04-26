package com.github.timmyovo.pixelbedwars.trap;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TrapSpeed extends Trap {
    @Override
    public void executeTrap(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1));
    }

    @Override
    public void tickTrap() {
        BedwarsGame bedwarsGame = PixelBedwars.getPixelBedwars()
                .getBedwarsGame();
        for (GamePlayer gamePlayer : bedwarsGame.getGamePlayers()) {
            if (checkTrap(gamePlayer.getPlayer())) {
            }
        }
    }

    private boolean checkTrap(Player player) {
        BedwarsGame bedwarsGame = PixelBedwars.getPixelBedwars()
                .getBedwarsGame();
        GameTeam playerTeam = bedwarsGame.getPlayerTeam(player);
        if (player.hasMetadata("IGNORE")) {
            return false;
        }
        for (GameTeam gameTeam : bedwarsGame.getTeamList()) {
            if (playerTeam.equals(gameTeam)) {
                continue;
            }
            if (!gameTeam.getTeamShoppingProperties().isSpeedupTrap()) {
                continue;
            }
            if (gameTeam.getTeamMeta().getTeamBedLocation().toBukkitLocation().distance(player.getLocation()) <= 8) {
                gameTeam.getAlivePlayers().stream().map(GamePlayer::getPlayer).forEach(this::executeTrap);
                gameTeam.getTeamShoppingProperties().setSpeedupTrap(false);
                return true;
            }
        }
        return false;
    }
}
