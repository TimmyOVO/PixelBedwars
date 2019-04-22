package com.github.timmyovo.pixelbedwars.settings;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.timmyovo.pixelbedwars.settings.item.RandomInventoryItemList;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameSetting {
    //玩家等待位置
    private VecLoc3D playerWaitLocation;
    //最少开始玩家
    private int minPlayer;
    //最多开始玩家
    private int maxPlayer;
    //开始等待时间
    private int waitTime;
    //玩家满后缩短到多少等待时间
    private int playerFullWaitTime;
    //游戏时长限制
    private int gameTime;

    private List<TeamMeta> teamMetaList;
    //重生冷却时间
    private int respawnCoolDown;
    //边界中心位置
    private VecLoc3D mapWorldCenter;
    //边界大小
    private int borderSize;
    //玩家最大血量
    private int playerMaxHealth;
    //是否开启玩家尸体
    private boolean playerCorpseEnable;
    //尸体消失速率
    private int playerCorpseDespawnRate;

    private ScoreboardConfiguration waitScoreboard;
    private ScoreboardConfiguration gamingScoreboard;
    private ScoreboardConfiguration endScoreboard;

    private List<RandomInventoryItemList> randomInventoryItemListList;


    private String motdWait;
    private String motdGaming;
    private String motdEnd;
    private List<String> hubServers;

    private String selectTeamItemType;
    private int selectTeamItemSlot;
    private String quitItemType;
    private int quitItemSlot;
    private int serverRestartDelay;

    private List<VecLoc3D> randomSpawnLocations;

    private boolean disableWeather;
    private boolean disableTimeCycle;
    private String counterSound;
}
