package com.github.timmyovo.pixelbedwars.trap;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import org.bukkit.entity.Player;

public class TrapNotification extends Trap {
    @Override
    public void executeTrap(Player player) {
        player.sendMessage("警报已经被触发!");
        PixelBedwars.getPixelBedwars().getBedwarsGame().getGamePlayers().forEach(gamePlayer -> {
            gamePlayer.getPlayer().showPlayer(player);
        });
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
            if (!gameTeam.getTeamShoppingProperties().isNotificationTrap()) {
                continue;
            }
            if (gameTeam.getTeamMeta().getTeamBedLocation().toBukkitLocation().distance(player.getLocation()) <= 8) {
                gameTeam.getAlivePlayers().stream().map(GamePlayer::getPlayer).forEach(this::executeTrap);
                gameTeam.getTeamShoppingProperties().setNotificationTrap(false);
                return true;
            }
        }
        return false;
    }
}
