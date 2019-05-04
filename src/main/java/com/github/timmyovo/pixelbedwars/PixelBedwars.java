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
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.github.timmyovo.pixelbedwars.database.PlayerQuickShopEntryModel;
import com.github.timmyovo.pixelbedwars.database.PlayerRejoinModel;
import com.github.timmyovo.pixelbedwars.database.PlayerStatisticModel;
import com.github.timmyovo.pixelbedwars.entity.BedwarsEgg;
import com.github.timmyovo.pixelbedwars.entity.BedwarsEnderDragon;
import com.github.timmyovo.pixelbedwars.entity.BedwarsGolem;
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
import com.github.timmyovo.pixelbedwars.settings.title.TitleEntry;
import com.github.timmyovo.pixelbedwars.shop.ConfigQuickShopGui;
import com.github.timmyovo.pixelbedwars.shop.PlayerShop;
import com.github.timmyovo.pixelbedwars.shop.ShopGui;
import com.github.timmyovo.pixelbedwars.shop.TeamShopGui;
import com.github.timmyovo.pixelbedwars.shop.category.ShopCategory;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import com.github.timmyovo.pixelbedwars.trap.TrapGui;
import com.github.timmyovo.pixelbedwars.utils.CommandsHelp;
import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Setter
public final class PixelBedwars extends JavaPlugin implements PluginInstance {
    private static PixelBedwars pixelBedwars;
    private CorpsesManager corpsesManager;
    private ConfigurationManager configurationManager;
    private GameSetting gameSetting;
    private Language language;
    private PlayerShop playerShop;
    private ShopGui playerShopGui;
    private ConfigQuickShopGui configQuickShopGui;
    private PlayerShop teamShop;
    private TeamShopGui teamShopGui;
    private BedwarsGame bedwarsGame;
    private DatabaseManager databaseManagerBase;
    private LoadingCache<Player, PlayerStatisticModel> playerPlayerStatisticModelLoadingCache;

    private String serverName = "default";


    public static PixelBedwars getPixelBedwars() {
        return pixelBedwars;
    }

    @Override
    public void onEnable() {
        PixelBedwars.pixelBedwars = this;
        if (new File(getDataFolder(), "rejoin.options").exists()) {
            initDatabase();
            MainCommandSpec.newBuilder()
                    .addAlias("rejoin")
                    .addAlias("rj")
                    .withCommandSpecExecutor((commandSender, strings) -> {
                        if (!(commandSender instanceof Player)) {
                            return true;
                        }
                        Player player = (Player) commandSender;
                        PlayerRejoinModel playerRejoinModel = PlayerRejoinModel.db()
                                .find(PlayerRejoinModel.class, player.getUniqueId());
                        if (playerRejoinModel == null) {
                            commandSender.sendMessage("无法找到上一局游戏可能已经结束或不可用!");
                            return true;
                        }
                        BedwarsGame.sendToServer(player, playerRejoinModel.getServerName());
                        return true;
                    })
                    .build()
                    .register();
            return;
        }
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, message) -> {
            if (!channel.equalsIgnoreCase("BungeeCord")) {
                return;
            }
            if (serverName == null || serverName.isEmpty()) {
                ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(message);
                System.out.println("channel: " + byteArrayDataInput.readUTF());
                setServerName(byteArrayDataInput.readUTF());
                getLogger().info("服务器名字自动设置完成!");
                getLogger().info("服务器名字设置为: " + serverName);
            }

        });
        new BukkitRunnable() {
            @Override
            public void run() {
                if (serverName == null || serverName.isEmpty()) {
                    getLogger().warning("警告: 插件正在等待玩家进入游戏初始化重连功能");
                    getLogger().warning("在此之前重连功能无法使用!");
                }
            }
        }.runTask(this);
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
            NMSUtils.registerEntity(BedwarsEgg.class, "BedwarsEgg", EntityType.EGG.getTypeId());
            NMSUtils.registerEntity(BedwarsGolem.class, "BedwarsGolem", EntityType.IRON_GOLEM.getTypeId());
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
        playerShopGui = new ShopGui(playerShop);
        Bukkit.getPluginManager().registerEvents(playerShopGui, this);
        teamShopGui = new TeamShopGui();
        Bukkit.getPluginManager().registerEvents(teamShopGui, this);
        configQuickShopGui = new ConfigQuickShopGui(playerShop);
        Bukkit.getPluginManager().registerEvents(configQuickShopGui, this);
        Bukkit.getPluginManager().registerEvents(new TrapGui(), this);
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
                    .withModelClass(Arrays.asList(PlayerStatisticModel.class, PlayerRejoinModel.class, PlayerQuickShopEntryModel.class))
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
        configurationManager.registerConfiguration("playerShop", () -> PlayerShop.builder()
                .entityType(EntityType.VILLAGER)
                .displayName("商店")
                .categoryItems(ImmutableMap.<Integer, ShopCategory>builder()
                        .put(0, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.NETHER_STAR))
                                                .setDisplayName("§b快速购买")
                                                .pack().serialize())
                                        .build())
                                .shopItemMap(ImmutableMap.<Integer, ShopItem>builder()
                                        .put(19, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.WOOL).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Collections.singletonList(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.WOOL).serialize())
                                                        .build()))
                                                .build())
                                        .put(20, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.STONE_SWORD).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Collections.singletonList(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.STONE_SWORD).serialize())
                                                        .build()))
                                                .build())
                                        .put(21, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.CHAINMAIL_BOOTS).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.CHAINMAIL_BOOTS).serialize())
                                                                .build(),
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.CHAINMAIL_LEGGINGS).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(22, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.WOOD_PICKAXE).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.WOOD_PICKAXE).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(23, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.BOW).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.BOW).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(24, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.POTION).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.POTION).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(25, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.TNT).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.TNT).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(28, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.WOOD).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.WOOD).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(29, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_SWORD).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.IRON_SWORD).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(30, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_BOOTS).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.IRON_BOOTS).serialize())
                                                                .build(),
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.IRON_LEGGINGS).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(31, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.SHEARS).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.SHEARS).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(32, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.ARROW).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.ARROW).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(33, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.POTION).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.POTION).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .put(34, ShopItem.builder()
                                                .icon(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.WATER_BUCKET).serialize())
                                                        .build())
                                                .requireItem(InventoryItem.builder()
                                                        .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                        .build())
                                                .items(Arrays.asList(
                                                        InventoryItem.builder()
                                                                .itemstackData(new ItemStack(Material.WATER_BUCKET).serialize())
                                                                .build()
                                                ))
                                                .build())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(1, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.HARD_CLAY))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a方块")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(2, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.STONE_SWORD))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a近战武器")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(3, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.CHAINMAIL_BOOTS))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a装备")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(4, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.STONE_PICKAXE))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a工具")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(5, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.BOW))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a远程类")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(6, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.BREWING_STAND_ITEM))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a药水类")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .put(7, ShopCategory.builder()
                                .icon(InventoryItem.builder()
                                        .itemstackData(new ItemFactory(() -> new ItemStack(Material.TNT))
                                                .addLore("§e点击查看!")
                                                .setDisplayName("§a实用道具")
                                                .pack().serialize())
                                        .build())
                                .requirePermission(null)
                                .build())
                        .build())
                .build());
        configurationManager.registerConfiguration("gameSetting", () -> {
            VecLoc3D defaultLocation = VecLoc3D.valueOf(defaultWorld.getSpawnLocation());
            return GameSetting.builder()
                    .chestProtectRadius(8)
                    .ironSpawnerList(Lists.newArrayList())
                    .goldSpawnerList(Lists.newArrayList())
                    .diamondSpawnerList(Lists.newArrayList())
                    .emeraldSpawnerList(Lists.newArrayList())
                    .bucketBuildProtectRadius(10)
                    .homeBuildProtectRadius(10)
                    .spawnerBuildProtectRadius(8)
                    .fireballExplodePower(10)
                    .tntExplodePower(10)
                    .fireballCooldown(0.5F)
                    .tntExplodeDelay(100)
                    .teamGuiEntityType(EntityType.ZOMBIE)
                    .playerShopEntityList(Lists.newArrayList())
                    .teamShopEntityList(Lists.newArrayList())
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
                                    .stageCommand("pb srrs EMERALD 2")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("绿宝石生成Ⅱ阶段")
                                    .stageCounter(120)
                                    .flow(2)
                                    .stageCommand("pb srrs EMERALD 3")
                                    .build(),
                            StageEntry.builder()
                                    .stageName("绿宝石生成Ⅲ阶段")
                                    .stageCounter(300)
                                    .flow(3)
                                    .stageCommand("pb srrs EMERALD 5")
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
                                    .build(),
                            StageEntry.builder()
                                    .stageName("游戏结束")
                                    .stageCounter(300)
                                    .flow(6)
                                    .stageCommand("pb stopGame")
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
                                    "破坏床数:&a%pb_info#bedDestroyed%",
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
                        .quickShopConfigGuiName("配置快捷购买")
                        .seizeIron("+%s个铁锭")
                        .seizeGold("+%s个金锭")
                        .seizeDiamond("+%s个钻石")
                        .seizeEmerald("+%s个绿宝石")
                        .breakBedDenied("你不能破坏自己队伍的床")
                        .canNotBreakBlock("你只能破坏人为放置的方块")
                        .respawnTitles(ImmutableMap.<Integer, TitleEntry>builder()
                                .put(1, TitleEntry.builder()
                                        .title("§c等待复活")
                                        .subtitle("§b%s")
                                        .build())
                                .put(2, TitleEntry.builder()
                                        .title("§c等待复活")
                                        .subtitle("§b%s")
                                        .build())
                                .put(3, TitleEntry.builder()
                                        .title("§c等待复活")
                                        .subtitle("§b%s")
                                        .build())
                                .put(4, TitleEntry.builder()
                                        .title("§c等待复活")
                                        .subtitle("§b%s")
                                        .build())
                                .put(5, TitleEntry.builder()
                                        .title("§c等待复活")
                                        .subtitle("§b%s")
                                        .build())
                                .build())
                        .teamShopDisplayName("队伍商店")
                        .fireballCoolingDownMessage("你还需要等待 %s 秒才能使用这个物品")
                        .teamShopHologramTexts(Arrays.asList("§6队伍商店", "§b右键点击"))
                        .playerShopHologramTexts(Arrays.asList("§6道具商店", "§b右键点击"))
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
        PlayerShop.initShopCommands();
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
                            getBedwarsGame().callDeathMatch();
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("srrs")
                        .addAlias("setResourceRefreshSpeed")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (strings.length == 2) {
                                try {
                                    getBedwarsGame().getGameSetting().setResourceSpawnerMultiplier(ResourceSpawner.SpawnerType.valueOf(strings[0]), Integer.valueOf(strings[1]));
                                } catch (NumberFormatException m) {
                                    commandSender.sendMessage("请输入数字");
                                } catch (EnumConstantNotPresentException n) {
                                    commandSender.sendMessage("无法找到指定资源");
                                }
                                return true;
                            }
                            commandSender.sendMessage("/pb setResourceRefreshSpeed [资源名称] [资源刷新乘数]");
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("strrs")
                        .addAlias("setTeamResourceRefreshSpeed")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (strings.length < 3) {
                                if (strings.length == 2) {
                                    try {
                                        getBedwarsGame().getTeamList()
                                                .stream()
                                                .map(GameTeam::getTeamMeta)
                                                .forEach(teamMeta -> {
                                                    teamMeta.setResourceSpawnerMultiplier(ResourceSpawner.SpawnerType.valueOf(strings[0]), Integer.valueOf(strings[1]));
                                                });
                                    } catch (NumberFormatException m) {
                                        commandSender.sendMessage("请输入数字");
                                    } catch (EnumConstantNotPresentException n) {
                                        commandSender.sendMessage("无法找到指定资源");
                                    }
                                    return true;
                                }
                                commandSender.sendMessage("/pb setTeamResourceRefreshSpeed [资源名称] [资源刷新乘数]");
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
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("destroyAllBed")
                        .addAlias("dab")
                        .withCommandSpecExecutor((commandSender, strings) -> {
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
                            if (strings.length < 2) {
                                commandSender.sendMessage("/pb addSpawner [资源名称] [刷新间隔(秒)]");
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                ResourceSpawner.SpawnerType spawnerType = ResourceSpawner.SpawnerType.valueOf(strings[0].toUpperCase());
                                ResourceSpawner resourceSpawner = new ResourceSpawner(Integer.valueOf(strings[1]), VecLoc3D.valueOf(player.getLocation()), spawnerType);
                                switch (spawnerType) {
                                    case IRON:
                                        gameSetting.getIronSpawnerList().add(resourceSpawner);
                                        break;
                                    case GOLD:
                                        gameSetting.getGoldSpawnerList().add(resourceSpawner);
                                        break;
                                    case DIAMOND:
                                        gameSetting.getDiamondSpawnerList().add(resourceSpawner);
                                        break;
                                    case EMERALD:
                                        gameSetting.getEmeraldSpawnerList().add(resourceSpawner);
                                        break;
                                }
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("addSpawnerTeam")
                        .addAlias("ast")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 3) {
                                commandSender.sendMessage("/pb addSpawnerTeam [队伍名称] [资源名称] [刷新间隔(秒)]");
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
                                save();
                                commandSender.sendMessage("成功!");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("ub")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                ItemStack itemInHand = player.getInventory().getItemInHand();
                                if (itemInHand != null) {
                                    ItemMeta itemMeta = itemInHand.getItemMeta();
                                    itemMeta.spigot().setUnbreakable(true);
                                    itemInHand.setItemMeta(itemMeta);
                                    player.setItemInHand(itemInHand);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("si")
                        .addAlias("saveitem")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                ItemStack itemInHand = player.getInventory().getItemInHand();
                                if (itemInHand != null) {
                                    String s = FileUtils.GSON.toJson(InventoryItem.builder()
                                            .itemstackData(itemInHand.serialize())
                                            .build());
                                    player.sendMessage(s);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("aps")
                        .addAlias("addPlayerShop")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                getGameSetting().getPlayerShopEntityList()
                                        .add(VecLoc3D.valueOf(player.getLocation()));
                                save();
                                player.sendMessage("成功");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("ats")
                        .addAlias("addTeamShop")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            try {
                                Player player = (Player) commandSender;
                                getGameSetting().getTeamShopEntityList()
                                        .add(VecLoc3D.valueOf(player.getLocation()));
                                save();
                                player.sendMessage("成功");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("stopGame")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            getBedwarsGame().endGame();
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("shopInit")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            getBedwarsGame().doShopInit();
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setShopItemIcon")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 2) {
                                commandSender.sendMessage("/pb setShopItemIcon 分类ID 格子ID");
                                return true;
                            }
                            Player player = (Player) commandSender;
                            ShopCategory shopCategory = getPlayerShop().getCategoryItems().get(Integer.valueOf(strings[0]));
                            if (shopCategory == null) {
                                commandSender.sendMessage("未找到指定分类");
                                return true;
                            }
                            if (shopCategory.getShopItemMap() == null) {
                                shopCategory.setShopItemMap(new HashMap<>());
                            }
                            Integer slotid = Integer.valueOf(strings[1]);
                            ShopItem shopItem = shopCategory.getShopItemMap().get(slotid);
                            if (shopItem == null) {
                                shopItem = ShopItem.builder()
                                        .icon(InventoryItem.builder()
                                                .itemstackData(new ItemStack(Material.WOOL).serialize())
                                                .build())
                                        .requireItem(InventoryItem.builder()
                                                .itemstackData(new ItemStack(Material.IRON_INGOT).serialize())
                                                .build())
                                        .items(Collections.singletonList(InventoryItem.builder()
                                                .itemstackData(new ItemStack(Material.WOOL).serialize())
                                                .build()))
                                        .build();
                            }

                            ItemStack itemInHand = player.getInventory().getItemInHand();
                            if (itemInHand != null) {
                                shopItem.setIcon(InventoryItem.builder()
                                        .itemstackData(itemInHand.serialize())
                                        .build());
                                shopCategory.getShopItemMap().put(slotid, shopItem);
                                save();
                                player.sendMessage("成功!");
                            } else {
                                player.sendMessage("请手持物品重试");
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setShopItemRequire")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 2) {
                                commandSender.sendMessage("/pb setShopItemRequire 分类ID 格子ID");
                                return true;
                            }
                            Player player = (Player) commandSender;
                            ShopCategory shopCategory = getPlayerShop().getCategoryItems().get(Integer.valueOf(strings[0]));
                            if (shopCategory == null) {
                                commandSender.sendMessage("未找到指定分类");
                                return true;
                            }

                            ShopItem shopItem = shopCategory.getShopItemMap().get(Integer.valueOf(strings[1]));
                            if (shopItem == null) {
                                player.sendMessage("未找到对应商品");
                                return true;
                            }

                            ItemStack itemInHand = player.getInventory().getItemInHand();
                            if (itemInHand != null) {
                                shopItem.setRequireItem(InventoryItem.builder()
                                        .itemstackData(itemInHand.serialize())
                                        .build());
                                save();
                                player.sendMessage("成功!");
                            } else {
                                player.sendMessage("请手持物品重试");
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("setShopItem")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            if (strings.length < 2) {
                                commandSender.sendMessage("/pb setShopItem 分类ID 格子ID");
                                return true;
                            }
                            Player player = (Player) commandSender;
                            ShopCategory shopCategory = getPlayerShop().getCategoryItems().get(Integer.valueOf(strings[0]));
                            if (shopCategory == null) {
                                commandSender.sendMessage("未找到指定分类");
                                return true;
                            }

                            ShopItem shopItem = shopCategory.getShopItemMap().get(Integer.valueOf(strings[1]));
                            if (shopItem == null) {
                                player.sendMessage("未找到对应商品");
                                return true;
                            }

                            ItemStack itemInHand = player.getInventory().getItemInHand();
                            if (itemInHand != null) {
                                List<InventoryItem> items = shopItem.getItems();
                                items.add(InventoryItem.builder()
                                        .itemstackData(itemInHand.serialize())
                                        .build());
                                shopItem.setItems(items);
                                save();
                                player.sendMessage("成功!");
                            } else {
                                player.sendMessage("请手持物品重试");
                            }
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("startGame")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            pixelBedwars.getBedwarsGame().startGame();
                            return true;
                        })
                        .build())
                .childCommandSpec(SubCommandSpec.newBuilder()
                        .addAlias("trap")
                        .withCommandSpecExecutor((commandSender, strings) -> {
                            if (!(commandSender instanceof Player)) {
                                return true;
                            }
                            new TrapGui().show(((Player) commandSender));
                            return true;
                        })
                        .build())
                .withCommandSpecExecutor((commandSender, strings) -> {
                    CommandsHelp.help(commandSender);
                    return true;
                })
                .build()
                .register();
    }

    private void save() {
        this.configurationManager.saveFiles();
    }
}
