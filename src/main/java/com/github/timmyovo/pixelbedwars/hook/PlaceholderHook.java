package com.github.timmyovo.pixelbedwars.hook;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.database.PlayerStatisticModel;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GamePlayer;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.settings.Language;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlaceholderHook extends EZPlaceholderHook {
    public PlaceholderHook(Plugin plugin) {
        super(plugin, "pb");
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {
        Language language = PixelBedwars.getPixelBedwars().getLanguage();
        BedwarsGame bedwarsGame = PixelBedwars.getPixelBedwars().getBedwarsGame();
        if (s.equalsIgnoreCase("nextstage")) {
            return bedwarsGame.getNextStage().getStageName();
        }
        if (s.equalsIgnoreCase("nextstage_time")) {
            return formatSeconds(bedwarsGame.getNextStage().getCounter());
        }
        if (s.equalsIgnoreCase("waittime")) {
            return String.valueOf(bedwarsGame.getGameStartCounter());
        }
        if (s.equalsIgnoreCase("gameplayers")) {
            return String.valueOf(bedwarsGame.getGamePlayers().size());
        }
        if (s.equalsIgnoreCase("maxplayer")) {
            return String.valueOf(bedwarsGame.getGameSetting().getMaxPlayer());
        }
        if (s.equalsIgnoreCase("kd")) {
            PlayerStatisticModel playerStatistic = PixelBedwars.getPixelBedwars().getPlayerStatistic(player);
            if (playerStatistic == null) {
                return String.valueOf(0);
            }
            int death = playerStatistic.getDeath();
            int kd;
            if (death == 0) {
                kd = 0;
                return String.valueOf(kd);
            }
            return String.valueOf(playerStatistic.getKills() / death);
        }
        if (s.equalsIgnoreCase("needplayer")) {
            int i = bedwarsGame.getGameSetting().getMinPlayer() - bedwarsGame.getGamePlayers().size();
            if (i > 0) {
                return language.getWaitPlayer().replace("%num%", String.valueOf(i));
            }
            return language.getGamePlayerEnough();
        }
        if (s.equalsIgnoreCase("team")) {
            GameTeam playerTeam = bedwarsGame.getPlayerTeam(player);
            if (playerTeam == null) {
                return "未选择";
            }
            return playerTeam.getTeamMeta().getFormatTeamName();
        }
        if (s.startsWith("status")) {
            if (s.contains("#")) {
                String[] split = s.split("#");
                try {
                    String teamName = split[1];
                    GameTeam teamByName = bedwarsGame.getTeamByName(teamName);
                    StringBuilder result = new StringBuilder("§l");

                    if (bedwarsGame.isTeamDead(teamByName)) {
                        result.append(ChatColor.RED);
                        result.append("✘");
                    }
                    if (teamByName.isBedDestroyed()) {
                        result.append(ChatColor.GREEN);
                        result.append(String.valueOf(teamByName.getAlivePlayers().size()));
                    } else {
                        result.append(ChatColor.GREEN);
                        result.append("✔");
                    }
                    if (bedwarsGame.getPlayerTeam(player).equals(teamByName)) {
                        result.append(ChatColor.GRAY);
                        result.append("你");
                    }
                    return result.toString();
                } catch (NullPointerException e) {
                    return "#ERROR_WHEN_FIND_TEAM";
                }
            }
        }
        if (s.startsWith("info")) {
            if (s.contains("#")) {
                String[] split = s.split("#");
                String infoField = split[1];
                GamePlayer playerStatistic = PixelBedwars.getPixelBedwars().getBedwarsGame().getBedwarsPlayer(player);
                if (playerStatistic == null) {
                    return "#ERROR_INFO";
                }
                try {
                    Field declaredField = playerStatistic.getClass().getDeclaredField(infoField);
                    declaredField.setAccessible(true);
                    Object value = declaredField.get(playerStatistic);
                    if (value instanceof UUID) {
                        return ((UUID) value).toString();
                    }
                    return value.toString();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return "#ERROR_GETTING_FIELD";
                }
            }
            //todo 写完阶段刷新
            //todo 写完商店系统
            //todo 写完阶段变量
        }
        return null;
    }

    private String formatSeconds(int seconds) {
        int ii = seconds % 60;
        int minutes = (seconds - ii) / 60;
        return minutes + ":" + ii;
    }
}
