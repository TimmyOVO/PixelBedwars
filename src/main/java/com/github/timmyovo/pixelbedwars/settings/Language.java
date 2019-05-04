package com.github.timmyovo.pixelbedwars.settings;

import com.github.timmyovo.pixelbedwars.settings.title.TitleEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Language {
    private String gameStart;
    private String gameCountingMessage;
    private String playerJoinMessage;
    private String playerQuitMessage;
    private String playerKillOthersMessage;
    private String playerSuicideMessage;
    private String gameEndMessage;
    private String teamItemName;
    private List<String> teamItemLore;

    private String playerDestroyBedMessage;
    private String allBedHasBeenDestroyed;

    private String teamWinMessage;
    private String serverRestartMessage;

    private String canNotSwitchGameBalance;
    private String canNotSwitchToCurrent;

    private String slimeBallName;
    private List<String> slimeBallLore;
    private String joinTeamMessage;
    private String quitItemName;
    private List<String> quitItemLore;

    private String gamePlayerEnough;
    private String waitPlayer;

    private String playerChatFormat;
    private String waitActionbar;
    private String gamingActionbar;
    private String endActionbar;

    private String hasBuyThatTeamItem;
    private String teamItemAvailable;
    private String requireMoreItemsToBuy;
    private String teamShopDisplayName;

    private List<String> teamShopHologramTexts;
    private List<String> playerShopHologramTexts;

    private Map<Integer, TitleEntry> respawnTitles;
    private String fireballCoolingDownMessage;

    private String canNotBreakBlock;
    private String breakBedDenied;
    private String seizeDiamond;
    private String seizeIron;
    private String seizeGold;
    private String seizeEmerald;
    private String quickShopConfigGuiName;
}
