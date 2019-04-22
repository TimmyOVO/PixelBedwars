package com.github.timmyovo.pixelbedwars.game;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.settings.Language;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode(of = {"teamMeta"})
@AllArgsConstructor
@NoArgsConstructor
public class GameTeam {
    private BedwarsGame bedwarsGame;
    private TeamMeta teamMeta;
    private Team team;

    public void addPlayer(Player player) {
        Validate.notNull(player);
        if (team.getSize() + 1 > teamMeta.getMaxPlayer()) {
            return;
        }
        PixelBedwars pixelBedwars = PixelBedwars.getPixelBedwars();
        BedwarsGame bedwarsGame = pixelBedwars.getBedwarsGame();
        Language language = pixelBedwars.getLanguage();
        GamePlayer gamePlayer = bedwarsGame.getBedwarsPlayer(player);
        if (gamePlayer == null) {
            return;
        }
        bedwarsGame.sendMessage(gamePlayer, language.getJoinTeamMessage(), ImmutableMap.of("%team_name%", teamMeta.getTeamName()));
        Objects.requireNonNull(team).addEntry(player.getName());
        String name = teamMeta.getTeamColor() + "[" + teamMeta.getTeamName() + "]" + player.getName();
        player.setDisplayName(name);
        player.setPlayerListName(name);
    }

    public void removePlayer(Player player) {
        Validate.notNull(player);
        Objects.requireNonNull(team).removeEntry(player.getName());
    }

    public boolean hasPlayer(Player player) {
        Validate.notNull(player);
        return Objects.requireNonNull(team).hasEntry(player.getName());
    }

    public List<GamePlayer> getTeamPlayers() {
        return getBedwarsGame().getGamePlayers()
                .stream()
                .filter(gamePlayer -> {
                    GameTeam playerTeam = bedwarsGame.getPlayerTeam(gamePlayer);
                    return playerTeam.equals(this);
                })
                .collect(Collectors.toList());
    }

    public List<GamePlayer> getAlivePlayers() {
        return getTeamPlayers()
                .stream()
                .filter(gamePlayer -> !gamePlayer.isTotallyDeath())
                .collect(Collectors.toList());
    }

    public boolean isBedDestroyed() {
        return getTeamMeta().getTeamBedLocation().toBukkitLocation().getBlock().getType() != Material.BED_BLOCK;
    }


}
