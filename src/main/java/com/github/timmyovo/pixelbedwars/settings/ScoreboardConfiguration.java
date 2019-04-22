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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreboardConfiguration {
    private boolean isEnable;
    private String displayName;
    private List<String> lines;

    public void show(Player player) {
        HashMap<String, Integer> stringHashMap = new HashMap<>();
        ArrayList<String> strings = new ArrayList<>(lines);
        Collections.reverse(strings);
        for (String line : strings) {
            int index = strings.indexOf(line);
            if (!line.isEmpty()) {
                line = ChatColor.translateAlternateColorCodes('&', line);
                line = PlaceholderAPI.setPlaceholders(player, line);
                stringHashMap.put(line, index + 1);
            }
        }
        ScoreboardUtils.rankedSidebarDisplay(player, displayName, stringHashMap);
    }

    public void hide(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
