package com.github.timmyovo.pixelbedwars.game;

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
    private boolean respawning;
    private boolean totallyDeath;

    public GamePlayer(Player player) {
        this.player = player;
        this.kills = 0;
        this.death = 0;
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
}
