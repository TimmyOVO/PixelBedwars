package com.github.timmyovo.pixelbedwars;

import com.github.skystardust.ultracore.bukkit.commands.MainCommandSpec;
import com.github.skystardust.ultracore.bukkit.commands.SubCommandSpec;
import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
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
import com.github.timmyovo.pixelbedwars.entity.CorpsesManager;
import com.github.timmyovo.pixelbedwars.game.BedwarsGame;
import com.github.timmyovo.pixelbedwars.hook.PlaceholderAPI;
import com.github.timmyovo.pixelbedwars.settings.GameSetting;
import com.github.timmyovo.pixelbedwars.settings.Language;
import com.github.timmyovo.pixelbedwars.settings.ScoreboardConfiguration;
import com.github.timmyovo.pixelbedwars.settings.item.RandomInventoryItem;
import com.github.timmyovo.pixelbedwars.settings.item.RandomInventoryItemList;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

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
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessageListener() {
            @Override
            public void onPluginMessageReceived(String s, Player player, byte[] bytes) {

            }
        });
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
                            playerStatisticModel = new PlayerStatisticModel(player.getUniqueId(), 0, 0, 0, 0);
                            databaseManagerBase.getEbeanServer().save(playerStatisticModel);
                            return playerStatisticModel;
                        }
                        return playerStatisticModel;
                    }
                });
        World defaultWorld = Bukkit.getWorlds().get(0);
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
            return;
        } catch (DatabaseInitException e) {
            getLogger().warning("Database init error with " + e.getLocalizedMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        configurationManager.registerConfiguration("gameSetting", () -> {
            VecLoc3D defaultLocation = VecLoc3D.valueOf(defaultWorld.getSpawnLocation());
            return GameSetting.builder()
                    .playerWaitLocation(defaultLocation)
                    .minPlayer(2)
                    .maxPlayer(10)
                    .waitTime(30)
                    .playerFullWaitTime(10)
                    .gameTime(600)
                    .randomSpawnLocations(Lists.newArrayList())
                    .teamMetaList(Arrays.asList(TeamMeta.builder()
                            .minPlayer(1)
                            .maxPlayer(4)
                            .teamColor(ChatColor.RED.toString())
                            .teamGameLocation(defaultLocation)
                            .teamName("红队")
                            .woolColor(DyeColor.RED.name())
                            .build(), TeamMeta.builder()
                            .minPlayer(1)
                            .maxPlayer(4)
                            .teamColor(ChatColor.BLUE.toString())
                            .teamGameLocation(defaultLocation)
                            .teamName("蓝队")
                            .woolColor(DyeColor.BLUE.name())
                            .build()))
                    .respawnCoolDown(5)
                    .mapWorldCenter(defaultLocation)
                    .borderSize(100)
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
                    .waitScoreboard(ScoreboardConfiguration.builder()
                            .displayName("等待中")
                            .isEnable(true)
                            .lines(Arrays.asList("游戏人数: %gb_gameplayers%/%gb_maxplayer%", " %gb_needplayer% "))
                            .build())
                    .gamingScoreboard(ScoreboardConfiguration.builder()
                            .displayName("游戏中")
                            .isEnable(true)
                            .lines(Arrays.asList("游戏人数: %gb_gameplayers%", "游戏中", "剩余时间: %gb_time%", "重生等待时间: %gb_respawntime%"))
                            .build())
                    .endScoreboard(ScoreboardConfiguration.builder()
                            .displayName("游戏结束")
                            .isEnable(true)
                            .lines(Arrays.asList("游戏结束", "正在等待传送"))
                            .build())
                    .randomInventoryItemListList(Arrays.asList(
                            RandomInventoryItemList.builder()
                                    .randomInventoryItemList(Collections.singletonList(RandomInventoryItem.builder()
                                            .inventoryItem(InventoryItem.builder()
                                                    .itemstackData(new ItemStack(Material.WOOD_SWORD).serialize())
                                                    .build())
                                            .build()))
                                    .chance(100)
                                    .build(),
                            RandomInventoryItemList.builder()
                                    .randomInventoryItemList(Collections.singletonList(RandomInventoryItem.builder()
                                            .inventoryItem(InventoryItem.builder()
                                                    .itemstackData(new ItemStack(Material.LEATHER_CHESTPLATE).serialize())
                                                    .build())
                                            .build()))
                                    .chance(100)
                                    .build()
                    ))
                    .motdWait("等待中")
                    .motdGaming("游戏中")
                    .motdEnd("游戏结束")
                    .build();
        })
                .registerConfiguration("language", () -> Language.builder()
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
                        .playerChatFormat("[Prefix][%player_name%][%gb_team%]: %s")
                        .waitActionbar("[%gb_gameplayers%]/[%gb_maxplayer%] -- 还需要 %gb_needplayer% -- 队伍: %gb_team%")
                        .gamingActionbar("undefined")
                        .endActionbar("undefined")
                        .build())
                .init(PixelBedwars.class, this)
                .start();
        new PlaceholderAPI(this).hook();
        this.bedwarsGame = new BedwarsGame();
        bedwarsGame.loadGame(gameSetting);
        InventoryBuilder inventoryBuilder = InventoryBuilder.builder()
                .displayName("队伍选择")
                .size(9)
                .itemMap(new HashMap<>())
                .build();
        this.getBedwarsGame().getTeamList()
                .forEach(team -> {
                    List<String> collect = language.getTeamItemLore().stream()
                            .map(string -> string.replace("%team_name%", team.getTeamMeta().getTeamName()))
                            .collect(Collectors.toList());
                    ItemStack pack = new ItemFactory(() -> new ItemStack(Material.WOOL))
                            .setDisplayName(team.getTeamMeta().getTeamColor() + team.getTeamMeta().getTeamName())
                            .setLore(collect)
                            .pack();
                    ((Wool) pack.getData())
                            .setColor(DyeColor.valueOf(team.getTeamMeta().getWoolColor()));
                    inventoryBuilder.addItem(pack
                    );
                });
        inventoryBuilder.fillWith(new ItemStack(Material.STAINED_GLASS_PANE))
                .onClickListener((i, itemStack, player) -> {
                    if (itemStack.getItemMeta().hasLore() && itemStack.getItemMeta().hasDisplayName()) {
                        String displayName = itemStack.getItemMeta().getDisplayName();
                        displayName = displayName.replaceAll("§.", "");
                        this.getBedwarsGame().requestSwitchTeamByName(player, displayName);
                    }
                })
                .openWith(new ItemFactory(() -> new ItemStack(Material.valueOf(gameSetting.getSelectTeamItemType()))).setDisplayName(language.getSlimeBallName()).setLore(language.getSlimeBallLore()).pack())
                .lock();
        registerCommands();
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
                .addAlias("gb")
                .addAlias("gameskywars")
                .withDescription("PixelBedwars main command")
                .withPermission("gameskywars.*")
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setBorderCenter")
                        .addAlias("sbc")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                getGameSetting().setMapWorldCenter(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("addRandomSpawnLocation")
                        .addAlias("arsl")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                getGameSetting().getRandomSpawnLocations().add(VecLoc3D.valueOf(((Player) commandSender).getLocation()));
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
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
                .withCommandSpecExecutor((commandSender, strings) -> {
                    commandSender.sendMessage("/gb setBorderCenter/sbc - 设置边界中心点");
                    commandSender.sendMessage("/gb addRandomSpawnLocation/arsl - 添加一个随机生成点");
                    commandSender.sendMessage("/gb addRandomItem/ari create [组名字] [几率] - 创建一个随机物品集合");
                    commandSender.sendMessage("/gb addRandomItem/ari add [组名字] - 添加手上物品到指定随机物品集合");
                    commandSender.sendMessage("/gb setWaitLoc/swl - 设置大厅位置");
                    commandSender.sendMessage("/gb setTeamGameLoc/stgl [队伍名字] - 设置队伍出生点为当前位置");
                    return true;
                })
                .build()
                .register();
    }

    private void save() {
        this.configurationManager.saveFiles();
    }
}
