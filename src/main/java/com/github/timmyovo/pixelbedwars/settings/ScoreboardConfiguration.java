package com.github.timmyovo.pixelbedwars.settings;

import com.github.timmyovo.pixelbedwars.utils.ScoreboardUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreboardConfiguration {
    private boolean isEnable;
    private String displayName;
    private List<String> lines;

    public void show(Player player) {
        ScoreboardUtils.rankedSidebarDisplay(player, displayName, lines.stream()
                .filter(string -> !string.isEmpty())
                .map(string -> ChatColor.translateAlternateColorCodes('&', string))
                .map(string -> PlaceholderAPI.setPlaceholders(player, string))
                .collect(Collectors.toMap(
                        String::toString, string -> lines.indexOf(string)
                )));
    }

    public void hide(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
