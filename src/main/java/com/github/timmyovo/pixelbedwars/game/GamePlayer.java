package com.github.timmyovo.pixelbedwars.game;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GamePlayer {
    private Player player;
    private int kills;
    private int death;
    private int finalKills;
    private int bedDestroyed;
    private boolean respawning;
    private boolean totallyDeath;

    public GamePlayer(Player player) {
        this.player = player;
        this.kills = 0;
        this.death = 0;
        this.finalKills = 0;
        this.bedDestroyed = 0;
        this.respawning = false;
        this.totallyDeath = false;
    }

    public void addDeath() {
        this.death++;
    }

    public void addKill() {
        this.kills++;
    }

    public boolean isPlayerEqual(Player player) {
        Validate.notNull(player);
        return this.player.getUniqueId().equals(player.getUniqueId());
    }

    public GameTeam getPlayerTeam() {
        return PixelBedwars.getPixelBedwars().getBedwarsGame().getPlayerTeam(this);
    }

    public void addFinalKills() {
        this.finalKills++;
    }

    public void addBedDestroyed() {
        this.bedDestroyed++;
    }
}
