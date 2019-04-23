package com.github.timmyovo.pixelbedwars;

import com.github.skystardust.ultracore.bukkit.commands.MainCommandSpec;
import com.github.skystardust.ultracore.bukkit.commands.SubCommandSpec;
import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.skystardust.ultracore.bukkit.modules.inventory.InventoryBuilder;
import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.configuration.ConfigurationManager;
import com.github.skystardust.ultracore.core.configuration.SQLConfiguration;
import com.github.skystardust.ultracore.core.database.DatabaseManagerBase;
import com.github.skystardust.ultracore.core.database.newgen.DatabaseManager;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.exceptions.DatabaseInitException;
import com.github.timmyovo.pixelbedwars.database.PlayerStatisticModel;
import com.github.timmyovo.pixelbedwars.entity.BedwarsEnderDragon;
import com.github.timmyovo.pixelbedwars.entity.CorpsesManager;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.hook.PlaceholderHook;
import com.github.timmyovo.pixelbedwars.settings.GameSetting;
import com.github.timmyovo.pixelbedwars.settings.Language;
import com.github.timmyovo.pixelbedwars.settings.ScoreboardConfiguration;
import com.github.timmyovo.pixelbedwars.settings.resource.ResourceSpawner;
import com.github.timmyovo.pixelbedwars.settings.stage.StageEntry;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
public final class PixelBedwars extends JavaPlugin implements PluginInstance {
    private static PixelBedwars pixelBedwars;
    private CorpsesManager corpsesManager;
    private ConfigurationManager configurationManager;
    private GameSetting gameSetting;
    private Language language;
    private BedwarsGame bedwarsGame;
    private DatabaseManager databaseManagerBase;
    private LoadingCache<Player, PlayerStatisticModel> playerPlayerStatisticModelLoadingCache;

    public static PixelBedwars getPixelBedwars() {
        return pixelBedwars;
    }

    @Override
    public void onEnable() {
        PixelBedwars.pixelBedwars = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (s, player, bytes) -> {

        });
        boot();
        if (!initDatabase()) {
            return;
        }
        initConfigurations();
        loadCustomEntity();
        new PlaceholderHook(this).hook();
        this.bedwarsGame = new BedwarsGame().loadGame(gameSetting);
        initGui();
        registerCommands();
    }

    private void loadCustomEntity() {
        try {
            NMSUtils.registerEntity(BedwarsEnderDragon.class, "BedwarsEnderDragon", EntityType.ENDER_DRAGON.getTypeId());
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void boot() {
        this.corpsesManager = new CorpsesManager();
        configurationManager = new ConfigurationManager(this);
        this.playerPlayerStatisticModelLoadingCache = CacheBuilder
                .newBuilder()
                .weakKeys()
                .build(new CacheLoader<Player, PlayerStatisticModel>() {
                    @Override
                    @ParametersAreNonnullByDefault
                    public PlayerStatisticModel load(Player player) {
                        PlayerStatisticModel playerStatisticModel = databaseManagerBase.getEbeanServer()
                                .find(PlayerStatisticModel.class)
                                .where()
                                .eq("uuid", player.getUniqueId())
                                .findOne();
                        if (playerStatisticModel == null) {
                            playerStatisticModel = new PlayerStatisticModel(player.getUniqueId(), 0, 0, 0, 0, 0, 0);
                            databaseManagerBase.getEbeanServer().save(playerStatisticModel);
                            return playerStatisticModel;
                        }
                        return playerStatisticModel;
                    }
                });
    }

    private void initGui() {
        InventoryBuilder inventoryBuilder = InventoryBuilder.builder()
                .displayName("队伍选择")
                .size(9)
                .itemMap(new HashMap<>())
                .updateMenu(param -> {
                    HashMap<Integer, ItemStack> result = new HashMap<>();
                    final int[] id = {0};
                    this.getBedwarsGame().getTeamList()
                            .forEach(team -> {
                                List<String> collect = language.getTeamItemLore().stream()
                                        .map(string -> string.replace("%team_name%", team.getTeamMeta().getTeamName()))
                                        .collect(Collectors.toList());
                                ItemStack pack = new ItemFactory(() -> new ItemStack(Material.WOOL))
                                        .setDisplayName(team.getTeamMeta().getTeamColor() + team.getTeamMeta().getTeamName())
                                        .setLore(collect)
                                        .pack();
                                pack.setDurability(DyeColor.valueOf(team.getTeamMeta().getWoolColor()).getWoolData());
                                result.put(id[0], pack);
                                id[0]++;
                            });
                    return result;
                })
                .build()
                .onClickListenerAdv(inventoryClickEvent -> {
                    Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
                    ItemStack item = NMSUtils.fixItemStackMeta(clickedInventory.getItem(inventoryClickEvent.getSlot()));
                    Player whoClicked = (Player) inventoryClickEvent.getWhoClicked();
                    if (item.getItemMeta().hasLore() && item.getItemMeta().hasDisplayName()) {
                        String displayName = item.getItemMeta().getDisplayName();
                        displayName = displayName.replaceAll("§.", "");
                        this.getBedwarsGame().requestSwitchTeamByName(whoClicked, displayName);
                    }
                })
                .openWith(new ItemFactory(() -> new ItemStack(Material.valueOf(gameSetting.getSelectTeamItemType()))).setDisplayName(language.getSlimeBallName()).setLore(language.getSlimeBallLore()).pack())
                .lock();
    }

    private boolean initDatabase() {
        try {
            SQLConfiguration sqlConfiguration = DatabaseManagerBase.setupDatabase(this);
            DatabaseManager gamebattle_database = DatabaseManager.newBuilder()
                    .withName("gamebattle_database")
                    .withOwnerPlugin(this)
                    .withModelClass(Arrays.asList(PlayerStatisticModel.class))
                    .withSqlConfiguration(sqlConfiguration)
                    .build();
            gamebattle_database
                    .openConnection();
            this.databaseManagerBase = gamebattle_database;
        } catch (ConfigurationException e) {
            getLogger().warning("Init configuration file failed,please try again");
            getLogger().warning(e.getLocalizedMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        } catch (DatabaseInitException e) {
            getLogger().warning("Database init error with " + e.getLocalizedMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private void initConfigurations() {
        World defaultWorld = Bukkit.getWorlds().get(0);
        configurationManager.registerConfiguration("gameSetting", () -> {
            VecLoc3D defaultLocation = VecLoc3D.valueOf(defaultWorld.getSpawnLocation());
            return GameSetting.builder()
                    .playerWaitLocation(defaultLocation)
                    .minPlayer(2)
                    .maxPlayer(10)
                    .waitTime(30)
                    .playerFullWaitTime(10)
                    .teamMetaList(Arrays.asList(
                            TeamMeta.builder()
                                    .minPlayer(1)
                                    .maxPlayer(4)
                                    .teamColor(ChatColor.BLUE.toString())
                                    .teamGameLocation(defaultLocation)
                                    .teamName("蓝队")
                                    .woolColor(DyeColor.BLUE.name())
                                    .goldSpawnerList(Lists.newArrayList())
                                    .diamondSpawnerList(Lists.newArrayList())
                                    .emeraldSpawnerList(Lists.newArrayList())
                                    .ironSpawnerList(Lists.newArrayList())
                                    .build()))
                    .respawnCoolDown(5)
                    .playerRespawnWaitLocation(defaultLocation)
                    .playerMaxHealth(40)
                    .playerCorpseEnable(true)
                    .playerCorpseDespawnRate(600)
                    .selectTeamItemType(Material.SLIME_BALL.name())
                    .selectTeamItemSlot(0)
                    .quitItemType(Material.SLIME_BALL.name())
                    .serverRestartDelay(5)
                    .quitItemSlot(1)
                    .disableWeather(true)
                    .disableTimeCycle(true)
                    .counterSound(Sound.ORB_PICKUP.name())
                    .hubServers(Arrays.asList("hub1", "hub2"))
                    .stageEntryList(Arrays.asList(
                            StageEntry.builder()
                                    .stageName("绿宝石生成Ⅰ阶段")
                                    .stageCounter(5)
                                    .flow(1)
                                    .stageCommand("pb strrs EMERALD 2")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("绿宝石生成Ⅱ阶段")
                                    .stageCounter(120)
                                    .flow(2)
                                    .stageCommand("pb strrs EMERALD 3")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("绿宝石生成Ⅲ阶段")
                                    .stageCounter(600)
                                    .flow(3)
                                    .stageCommand("pb strrs EMERALD 5")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("所有床自毁")
                                    .stageCounter(300)
                                    .flow(4)
                                    .stageCommand("pb dab")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("绝杀模式")
                                    .stageCounter(300)
                                    .flow(5)
                                    .stageCommand("pb dm")
                                    .build()
                    ))
                    .waitScoreboard(ScoreboardConfiguration.builder()
                            .displayName("起床战争")
                            .isEnable(true)
                            .lines(Arrays.asList(
                                    "&7%server_time_yy/MM/dd% &8mm1",
                                    "&c&l",
                                    "地图:&a测试",
                                    "玩家: &a%pb_gameplayers%/%pb_maxplayer%",
                                    "&c",
                                    "等待中:&a%pb_waittime% ",
                                    "&e",
                                    "模式:&a2v2",
                                    "版本:&a1.0-SNAPSHOT",
                                    "&a&c",
                                    "&ewww.example.com"
                                    )
                            )
                            .build())
                    .gamingScoreboard(ScoreboardConfiguration.builder()
                            .displayName("游戏中")
                            .isEnable(true)
                            .lines(Arrays.asList(
                                    "&7%server_time_yy/MM/dd% &8mm1",
                                    "&e&l",
                                    "%pb_nextstage% - &a %pb_nextstage_time%",
                                    "&c",
                                    "&c&l红 &r红队: %pb_status#红队%",
                                    "&9&l蓝 &r蓝队: %pb_status#蓝队%",
                                    "&e",
                                    "击杀数:&a%pb_info#kills%",
                                    "最终击杀数:&a%pb_info#finalKills%",
                                    "破坏床数:&a%pb_info#bedDestroy%",
                                    "&a&c",
                                    "&ewww.example.com"
                                    )
                            )
                            .build())
                    .endScoreboard(ScoreboardConfiguration.builder()
                            .displayName("游戏结束")
                            .isEnable(true)
                            .lines(Arrays.asList("游戏结束", "正在等待传送"))
                            .build())
                    .motdWait("等待中")
                    .motdGaming("游戏中")
                    .motdEnd("游戏结束")
                    .build();
        })
                .registerConfiguration("language", () -> Language.builder()
                        .playerDestroyBedMessage("玩家 %player% 摧毁了 %team% 的床!")
                        .allBedHasBeenDestroyed("所有床已经被摧毁")
                        .gameStart("游戏开始")
                        .gameCountingMessage("游戏还有 $s$ 开始!")
                        .playerJoinMessage("玩家 %player% 加入了游戏!")
                        .playerQuitMessage("玩家 %player% 退出了游戏!")
                        .playerKillOthersMessage("玩家 %killer% 击杀了玩家 %player%!")
                        .playerSuicideMessage("玩家 %player% 自杀了")
                        .gameEndMessage("游戏结束!")
                        .teamItemLore(Collections.singletonList("点击选择 %team_name%"))
                        .teamWinMessage("恭喜队伍 %team% 赢得比赛!")
                        .serverRestartMessage("服务器即将在 %sec% 后重启")
                        .canNotSwitchGameBalance("队伍人数不平衡,无法加入队伍")
                        .canNotSwitchToCurrent("无法切换到自己的队伍")
                        .slimeBallName("队伍选择")
                        .joinTeamMessage("你加入了 %team_name% !")
                        .slimeBallLore(Collections.singletonList("点击选择队伍"))
                        .gamePlayerEnough("游戏玩家已充足")
                        .waitPlayer("还需要 %num% 个玩家开始游戏")
                        .quitItemName("example")
                        .quitItemLore(Collections.singletonList("line1"))
                        .playerChatFormat("[Prefix][%player_name%][%pb_team%]: %s")
                        .waitActionbar("[%pb_gameplayers%]/[%pb_maxplayer%] -- 还需要 %pb_needplayer% -- 队伍: %pb_team%")
                        .gamingActionbar("undefined")
                        .endActionbar("undefined")
                        .build())
                .init(PixelBedwars.class, this)
                .start();
    }

    @Override
    public Logger getPluginLogger() {
        return getLogger();
    }

    public PlayerStatisticModel getPlayerStatistic(Player player) {
        try {
            return playerPlayerStatisticModelLoadingCache.get(player);
        } catch (ExecutionException e) {
            return null;
        }
    }

    public synchronized void setPlayerStatistic(Supplier<PlayerStatisticModel> playerStatisticModelSupplier) {
        PlayerStatisticModel playerStatisticModel = playerStatisticModelSupplier.get();
        databaseManagerBase.getEbeanServer().update(playerStatisticModel);
    }

    public void registerCommands() {
        MainCommandSpec.newBuilder()
                .addAlias("pb")
                .addAlias("pixelbedwars")
                .withDescription("PixelBedwars main command")
                .withPermission("pixelbedwars.*")
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setWorldSpawn")
                        .addAlias("sws")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                getGameSetting().setPlayerRespawnWaitLocation(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("deathMatch")
                        .addAlias("dm")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                getGameSetting().setPlayerRespawnWaitLocation(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("strrs")
                        .addAlias("setTeamResourceRefreshSpeed")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 3) {
                                if (strings.length == 2) {
                                    try {
                                        getBedwarsGame().getTeamList()
                                                .stream()
                                                .map(GameTeam::getTeamMeta)
                                                .forEach(teamMeta -> {
                                                    teamMeta.allSpawnerMultiplier(Integer.valueOf(strings[1]));
                                                });
                                    } catch (NumberFormatException m) {
                                        commandSender.sendMessage("请输入数字");
                                    } catch (EnumConstantNotPresentException n) {
                                        commandSender.sendMessage("无法找到指定资源");
                                    }
                                    return true;
                                }
                                commandSender.sendMessage("/pb setTeamResourceRefreshSpeed [队伍名称] [资源名称] [资源刷新乘数]");
                                return true;
                            }
                            try {
                                GameTeam teamByName = getBedwarsGame().getTeamByName(strings[0]);
                                teamByName.getTeamMeta().setResourceSpawnerMultiplier(ResourceSpawner.SpawnerType.valueOf(strings[1]), Integer.valueOf(strings[2]));
                            } catch (NullPointerException e) {
                                return true;
                            } catch (NumberFormatException m) {
                                commandSender.sendMessage("请输入数字");
                            } catch (EnumConstantNotPresentException n) {
                                commandSender.sendMessage("无法找到指定资源");
                            }
                            Player player = (Player) commandSender;
                            WorldServer handle = ((CraftWorld) player.getWorld()).getHandle();
                            Location l1 = player.getLocation();
                            EntityEnderDragon entityEnderDragon = new EntityEnderDragon(handle);
                            entityEnderDragon.setPosition(l1.getX(), l1.getY(), l1.getZ());
                            handle.addEntity(entityEnderDragon);
                            Bukkit.getScheduler().runTaskTimerAsynchronously(getPixelBedwars(), () -> {
                                entityEnderDragon.target = ((CraftPlayer) player).getHandle();
                            }, 0L, 1L);
                            return true;
                        })
                        .build())
                /*.childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("addRandomItem")
                        .addAlias("ari")
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("create")
                                .withCommandSpecExecutor((commandSender, strings) -> {
                                    if (!(commandSender instanceof Player)) {
                                        return true;
                                    }
                                    if (strings.length < 2) {
                                        return true;
                                    }
                                    Player player = (Player) commandSender;
                                    if (player.getItemInHand() == null) {
                                        return true;
                                    }
                                    if (getGameSetting().getRandomInventoryItemListList().stream().anyMatch(randomInventoryItemList -> randomInventoryItemList.getName().equals(strings[0]))) {
                                        commandSender.sendMessage("无法保存，已有相同名字的集合");
                                        return true;
                                    }
                                    try {
                                        getGameSetting().getRandomInventoryItemListList()
                                                .add(RandomInventoryItemList.builder()
                                                        .name(strings[0])
                                                        .chance(Integer.valueOf(strings[1]))
                                                        .randomInventoryItemList(Lists.newArrayList())
                                                        .build());
                                    } catch (NumberFormatException n) {
                                        commandSender.sendMessage("错误，几率不是数字！");
                                        return true;
                                    }
                                    save();
                                    commandSender.sendMessage("成功！");
                                    return true;
                                })
                                .build())
                        .childCommandSpec(SubCommandSpec.newBuilder()
                                .addAlias("add")
                                .withCommandSpecExecutor((commandSender, strings) -> {
                                    if (!(commandSender instanceof Player)) {
                                        return true;
                                    }
                                    if (strings.length < 1) {
                                        return true;
                                    }
                                    Player player = (Player) commandSender;
                                    if (player.getItemInHand() == null) {
                                        return true;
                                    }
                                    List<RandomInventoryItemList> collect = getGameSetting().getRandomInventoryItemListList()
                                            .stream()
                                            .filter(randomInventoryItemList -> randomInventoryItemList.getName().equals(strings[0]))
                                            .collect(Collectors.toList());
                                    if (collect.isEmpty()) {
                                        commandSender.sendMessage("未找到指定随机物品组！");
                                        return true;
                                    }
                                    getGameSetting().getRandomInventoryItemListList()
                                            .stream()
                                            .filter(randomInventoryItemList -> randomInventoryItemList.getName().equals(strings[0]))
                                            .forEach(randomInventoryItemList -> randomInventoryItemList.add(RandomInventoryItem.builder()
                                                    .inventoryItem(InventoryItem.builder()
                                                            .itemstackData(player.getItemInHand().serialize())
                                                            .build())
                                                    .build()));
                                    save();
                                    commandSender.sendMessage("成功！");
                                    return true;
                                })
                                .build())
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            commandSender.sendMessage("/gb addRandomItem/ari create [组名字] [几率] - 创建一个随机物品集合");
                            commandSender.sendMessage("/gb addRandomItem/ari add [组名字] - 添加手上物品到指定随机物品集合");
                            return true;
                        })
                        .build())*/
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("destroyAllBed")
                        .addAlias("dab")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            getBedwarsGame().destroyAllBed();
                            commandSender.sendMessage("成功!");
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setWaitLoc")
                        .addAlias("swl")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            getGameSetting().setPlayerWaitLocation(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                            save();
                            commandSender.sendMessage("成功!");
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setTeamGameLoc")
                        .addAlias("stgl")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 1) {
                                return true;
                            }
                            try {
                                TeamMeta aThrow = getGameSetting().getTeamMetaList()
                                        .stream()
                                        .filter(teamMeta -> teamMeta.getTeamName().equals(strings[0]))
                                        .findAny()
                                        .orElseThrow(NullPointerException::new);
                                aThrow.setTeamGameLocation(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("addTeam")
                        .addAlias("at")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 5) {
                                commandSender.sendMessage("/pb addTeam [队伍名字] [队伍颜色] [最小人数] [最大人数] [队伍颜色]");
                                return true;
                            }
                            try {
                                TeamMeta build = TeamMeta.builder()
                                        .teamName(strings[0])
                                        .teamColor(ChatColor.valueOf(strings[1]).toString())
                                        .minPlayer(Integer.valueOf(strings[2]))
                                        .maxPlayer(Integer.valueOf(strings[3]))
                                        .teamGameLocation(null)
                                        .woolColor(DyeColor.valueOf(strings[4]).name())
                                        .teamBedLocation(null)
                                        .ironSpawnerList(Lists.newArrayList())
                                        .goldSpawnerList(Lists.newArrayList())
                                        .diamondSpawnerList(Lists.newArrayList())
                                        .emeraldSpawnerList(Lists.newArrayList())
                                        .build();
                                getGameSetting().getTeamMetaList().add(build);
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setTeamBed")
                        .addAlias("stb")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 1) {
                                commandSender.sendMessage("/pb setTeamBed [队伍名称]");
                                return true;
                            }
                            try {
                                TeamMeta aThrow = getGameSetting().getTeamMetaList()
                                        .stream()
                                        .filter(teamMeta -> teamMeta.getTeamName().equals(strings[0]))
                                        .findAny()
                                        .orElseThrow(NullPointerException::new);
                                Location add = ((Player) commandSender).getLocation().getBlock().getLocation();
                                if (add.getBlock().getType() != Material.BED_BLOCK) {
                                    commandSender.sendMessage("必须站在床上设置!");
                                    return true;
                                }
                                aThrow.setTeamBedLocation(VecLoc3D.valueOf(add));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("addSpawner")
                        .addAlias("as")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 3) {
                                commandSender.sendMessage("/pb addSpawner [队伍名称] [资源名称] [刷新间隔(秒)]");
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                TeamMeta aThrow = getGameSetting().getTeamMetaList()
                                        .stream()
                                        .filter(teamMeta -> teamMeta.getTeamName().equals(strings[0]))
                                        .findAny()
                                        .orElseThrow(NullPointerException::new);
                                ResourceSpawner.SpawnerType spawnerType = ResourceSpawner.SpawnerType.valueOf(strings[1].toUpperCase());
                                ResourceSpawner resourceSpawner = new ResourceSpawner(Integer.valueOf(strings[2]), VecLoc3D.valueOf(player.getLocation()), spawnerType);
                                switch (spawnerType) {
                                    case IRON:
                                        aThrow.getIronSpawnerList().add(resourceSpawner);
                                        break;
                                    case GOLD:
                                        aThrow.getGoldSpawnerList().add(resourceSpawner);
                                        break;
                                    case DIAMOND:
                                        aThrow.getDiamondSpawnerList().add(resourceSpawner);
                                        break;
                                    case EMERALD:
                                        aThrow.getEmeraldSpawnerList().add(resourceSpawner);
                                        break;
                                }
                                aThrow.setTeamGameLocation(VecLoc3D.valueOf(player.getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .withCommandSpecExecutor((commandSender, strings) -> {
                    commandSender.sendMessage("/pb setWorldSpawn/sws - 设置重生点");
                    commandSender.sendMessage("/pb addRandomItem/ari create [组名字] [几率] - 创建一个随机物品集合");
                    commandSender.sendMessage("/pb addRandomItem/ari add [组名字] - 添加手上物品到指定随机物品集合");
                    commandSender.sendMessage("/pb setWaitLoc/swl - 设置大厅位置");
                    commandSender.sendMessage("/pb setTeamGameLoc/stgl [队伍名字] - 设置队伍出生点为当前位置");
                    return true;
                })
                .build()
                .register();
    }

    private void save() {
        this.configurationManager.saveFiles();
    }
}
