package com.github.timmyovo.pixelbedwars.utils;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScoreboardUtils {
    public static boolean rankedSidebarDisplay(Player p, String title, Map<String, Integer> elements) {
        try {
            title = ChatColor.translateAlternateColorCodes('&', title);
            title = cutRankedTitle(title);
            elements = cutRanked(elements);
            Scoreboard scoreboard = p.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard() || scoreboard.getObjectives().size() != 1) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                p.setScoreboard(scoreboard);
            }
            Scoreboard finalScoreboard = scoreboard;
            PixelBedwars.getPixelBedwars().getBedwarsGame().getTeamList()
                    .forEach(gameTeam -> {
                        Team finalScoreboardTeam = finalScoreboard.getTeam(gameTeam.getTeam().getName());
                        if (finalScoreboardTeam == null) {
                            Team team = finalScoreboard.registerNewTeam(gameTeam.getTeam().getName());
                            team.setAllowFriendlyFire(false);
                            team.setPrefix(gameTeam.getTeam().getPrefix());
                            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
                            gameTeam.getTeam().getEntries().forEach(team::addEntry);
                        } else {
                            finalScoreboardTeam.getEntries().forEach(finalScoreboardTeam::removeEntry);
                            gameTeam.getTeam().getEntries().forEach(finalScoreboardTeam::addEntry);
                        }
                    });
            if (scoreboard.getObjective(p.getUniqueId().toString().substring(0, 16)) == null) {
                scoreboard.registerNewObjective(p.getUniqueId().toString().substring(0, 16), "dummy");
                scoreboard.getObjective(p.getUniqueId().toString().substring(0, 16)).setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
            objective.setDisplayName(title);
            for (String string : elements.keySet()) {
                if (objective.getScore(string).getScore() != elements.get(string)) {
                    objective.getScore(string).setScore(elements.get(string));
                }
            }
            Set<String> stringSet = scoreboard.getEntries();
            for (String string : stringSet) {
                if (!elements.keySet().contains(string)) {
                    scoreboard.resetScores(string);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String cutRankedTitle(String title) {
        if (title == null)
            return "Unamed board";

        if (title.length() > 16)
            return title.substring(0, 16);

        return title;
    }

    public static Map<String, Integer> cutRanked(Map<String, Integer> content) {
        Map<String, Integer> elements = new HashMap<>();
        elements.putAll(content);

        while (elements.size() > 15) {
            String minimumKey = (String) elements.keySet().toArray()[0];
            int minimum = elements.get(minimumKey);

            for (String string : elements.keySet())
                if (elements.get(string) < minimum || (elements.get(string) == minimum && string.compareTo(minimumKey) < 0)) {
                    minimumKey = string;
                    minimum = elements.get(string);
                }

            elements.remove(minimumKey);
        }

        for (String string : new ArrayList<>(elements.keySet()))
            if (string != null)
                if (string.length() > 32) {
                    int value = elements.get(string);
                    elements.remove(string);
                    elements.put(string.substring(0, 32), value);
                }

        return elements;

    }
}
