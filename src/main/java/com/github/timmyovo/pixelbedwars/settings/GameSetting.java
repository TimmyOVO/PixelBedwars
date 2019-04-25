package com.github.timmyovo.pixelbedwars.settings;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.timmyovo.pixelbedwars.settings.stage.StageEntry;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;

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
    private List<TeamMeta> teamMetaList;
    //重生冷却时间
    private int respawnCoolDown;
    //边界中心位置
    private VecLoc3D playerRespawnWaitLocation;
    //玩家最大血量
    private int playerMaxHealth;
    //是否开启玩家尸体
    private boolean playerCorpseEnable;
    //尸体消失速率
    private int playerCorpseDespawnRate;
    //阶段设置
    private List<StageEntry> stageEntryList;

    private ScoreboardConfiguration waitScoreboard;
    private ScoreboardConfiguration gamingScoreboard;
    private ScoreboardConfiguration endScoreboard;

    private String motdWait;
    private String motdGaming;
    private String motdEnd;
    private List<String> hubServers;

    private String selectTeamItemType;
    private int selectTeamItemSlot;
    private String quitItemType;
    private int quitItemSlot;
    private int serverRestartDelay;

    private EntityType teamGuiEntityType;
    private List<VecLoc3D> playerShopEntityList;
    private List<VecLoc3D> teamShopEntityList;

    private boolean disableWeather;
    private boolean disableTimeCycle;
    private String counterSound;
    private float fireballCooldown;
    private int tntExplodeDelay;
    private int tntExplodePower;
    private int fireballExplodePower;
}
