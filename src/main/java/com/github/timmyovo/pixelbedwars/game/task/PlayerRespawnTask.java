package com.github.timmyovo.pixelbedwars.game.task;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import com.github.timmyovo.pixelbedwars.settings.title.TitleEntry;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerRespawnTask extends BukkitRunnable {
    private GamePlayer gamePlayer;
    private BedwarsGame bedwarsGame;
    private int counter;

    public PlayerRespawnTask(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        this.counter = 5;
        this.bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
    }

    @Override
    public void run() {
        if (counter == 5) {
            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            gamePlayer.getPlayer().teleport(bedwarsGame.getGameSetting().getPlayerRespawnWaitLocation().toBukkitLocation());
        }
        if (counter <= 0) {
            if (!gamePlayer.isTotallyDeath()) {
                PixelBedwars.getPixelBedwars().getBedwarsGame().onPlayerSuccessRespawn(gamePlayer.getPlayer());
            }
            cancel();
            return;
        }
        TitleEntry titleEntry = getBedwarsGame().getLanguage().getRespawnTitles().get(counter);
        if (titleEntry != null) {
            titleEntry.sendToPlayer(gamePlayer.getPlayer(), ImmutableMap.of("%s", String.valueOf(counter)));
        }
        this.counter--;

    }

    public void start() {
        runTaskTimer(PixelBedwars.getPixelBedwars(), 0L, 20L);
    }
}
