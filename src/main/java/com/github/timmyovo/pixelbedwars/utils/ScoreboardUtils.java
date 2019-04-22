package com.github.timmyovo.pixelbedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardUtils {
    public static boolean rankedSidebarDisplay(Player p, String title, Map<String, Integer> elements) {
        title = ChatColor.translateAlternateColorCodes('&', title);
        title = cutRankedTitle(title);
        elements = cutRanked(elements);
        Scoreboard scoreboard;
        if (Bukkit.getScoreboardManager().getMainScoreboard() != null) {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        } else {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        p.setScoreboard(scoreboard);
        String substring = p.getUniqueId().toString().substring(0, 16);
        Objective objective = scoreboard.getObjective(substring);
        if (objective != null) {
            objective.unregister();
        }
        objective = scoreboard.registerNewObjective(substring, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);
        for (Map.Entry<String, Integer> stringIntegerEntry : elements.entrySet()) {
            objective.getScore(stringIntegerEntry.getKey()).setScore(stringIntegerEntry.getValue());
        }
        return true;
        /*try {
            title = ChatColor.translateAlternateColorCodes('&', title);
            title = cutRankedTitle(title);
            elements = cutRanked(elements);
            if (p.getScoreboard() == null || p.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard() || p.getScoreboard().getObjectives().size() != 1) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            if (p.getScoreboard().getObjective(p.getUniqueId().toString().substring(0, 16)) == null) {
                p.getScoreboard().registerNewObjective(p.getUniqueId().toString().substring(0, 16), "dummy");
                p.getScoreboard().getObjective(p.getUniqueId().toString().substring(0, 16)).setDisplaySlot(DisplaySlot.SIDEBAR);
            }
            Objective objective = p.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
            objective.setDisplayName(title);
            for (String string : elements.keySet()) {
                if (objective.getScore(string).getScore() != elements.get(string)) {
                    objective.getScore(string).setScore(elements.get(string));
                }
            }
            Set<String> stringSet = p.getScoreboard().getEntries();
            for (String string : stringSet) {
                if (!elements.keySet().contains(string)) {
                    p.getScoreboard().resetScores(string);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/
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
                if (string.length() > 16) {
                    int value = elements.get(string);
                    elements.remove(string);
                    elements.put(string.substring(0, 16), value);
                }

        return elements;

    }
}
