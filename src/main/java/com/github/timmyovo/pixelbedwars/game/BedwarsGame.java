package com.github.timmyovo.pixelbedwars.game;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.skystardust.ultracore.core.database.newgen.DatabaseManager;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.database.PlayerStatisticModel;
import com.github.timmyovo.pixelbedwars.entity.*;
import com.github.timmyovo.pixelbedwars.game.task.PlayerRespawnTask;
import com.github.timmyovo.pixelbedwars.settings.GameSetting;
import com.github.timmyovo.pixelbedwars.settings.Language;
import com.github.timmyovo.pixelbedwars.settings.stage.StageEntry;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import com.github.timmyovo.pixelbedwars.shop.TeamShoppingProperties;
import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BedwarsGame implements Listener {
    private GameSetting gameSetting;
    private GameState gameState;
    private List<GameTeam> teamList;
    private List<GamePlayer> gamePlayers;
    private Scoreboard scoreboard;

    private BukkitTask gameStartCounterTask;
    private int gameStartCounter;

    private BukkitTask gameTickTask;

    private BukkitTask stageTickTask;
    private BukkitTask healthRegenTask;

    private List<BedwarsEnderDragon> bedwarsEnderDragons;

    private Language language;

    private Map<UUID, Corpses.CorpseData> playerCorpseDataMap;

    public static boolean checkTeamBedLocation(Block block, GameTeam gameTeam) {
        return checkTeamBedLocation(block, gameTeam.getTeamMeta().getTeamBedLocation().toBukkitLocation());
    }

    public static boolean checkTeamBedLocation(Block block, Location bedLocation) {
        World world = bedLocation.getWorld();
        Location blockLocation = block.getLocation();
        BlockPosition blockPosition = new BlockPosition(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        Location east = NMSUtils.locationFromBlockPosition(world, blockPosition.east());
        Location west = NMSUtils.locationFromBlockPosition(world, blockPosition.west());
        Location north = NMSUtils.locationFromBlockPosition(world, blockPosition.north());
        Location south = NMSUtils.locationFromBlockPosition(world, blockPosition.south());

        return !bedLocation.equals(east) && !bedLocation.equals(west) && !bedLocation.equals(north) && !bedLocation.equals(south) && !blockLocation.equals(bedLocation);
    }

    public static void addDefaultWeapon(Player player) {
        ItemStack itemStack = new ItemStack(Material.WOOD_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.spigot().setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);
        player.getInventory().addItem(itemStack);
    }

    public BedwarsGame loadGame(GameSetting gameSetting) {
        this.gameState = GameState.LOADING;
        this.gameSetting = gameSetting;
        this.language = PixelBedwars.getPixelBedwars().getLanguage();
        this.playerCorpseDataMap = new HashMap<>();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.bedwarsEnderDragons = new ArrayList<>();
        this.getGameSetting().getStageEntryList().forEach(StageEntry::init);
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRuleValue("doDaylightCycle", gameSetting.isDisableTimeCycle() ? "false" : "true");
        });
        Location location = gameSetting.getPlayerRespawnWaitLocation().toBukkitLocation();
        checkChunk(location);
        location.getWorld().getWorldBorder().reset();
        this.teamList = gameSetting.getTeamMetaList()
                .stream()
                .map(teamMeta -> {
                    Team team = scoreboard.registerNewTeam(teamMeta.getTeamName());
                    team.setAllowFriendlyFire(false);
                    team.setPrefix(teamMeta.getTeamColor() + teamMeta.getTeamName());
                    team.setNameTagVisibility(NameTagVisibility.ALWAYS);
                    return new GameTeam(this, teamMeta, team, new TeamShoppingProperties());
                })
                .collect(Collectors.toList());
        this.gameStartCounter = gameSetting.getWaitTime();
        this.gamePlayers = Lists.newArrayList();
        checkChunk(gameSetting.getPlayerWaitLocation()
                .toBukkitLocation());
        this.stageTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (getGameState() != GameState.GAMING) {
                    return;
                }
                try {
                    getNextStage().tick();
                } catch (NullPointerException ignored) {
                }
            }
        }.runTaskTimer(PixelBedwars.getPixelBedwars(), 0L, 20L);
        this.gameStartCounterTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (getGameState() != GameState.WAITING) {
                    return;
                }

                if (canGameStart()) {
                    gameStartCounter--;
                    BedwarsGame.this.getGamePlayers()
                            .stream()
                            .map(GamePlayer::getPlayer)
                            .forEach(player -> {
                                float v = (((float) gameStartCounter) / ((float) gameSetting.getWaitTime()));
                                player.setExp(v);
                                player.setLevel(gameStartCounter);
                            });
                    updateScoreboard();
                    if (gameStartCounter <= 5) {
                        BedwarsGame.this.getGamePlayers()
                                .stream()
                                .map(GamePlayer::getPlayer)
                                .forEach(player -> {
                                    player.playSound(player.getLocation(), Sound.valueOf(gameSetting.getCounterSound()), 1, 1);
                                });
                        broadcastTitle(ChatColor.AQUA.toString() + gameStartCounter);
                    }
                    if (gameStartCounter <= 0) {
                        startGame();
                        cancel();
                    }
                } else {
                    gameStartCounter = gameSetting.getWaitTime();
                    updateScoreboard();
                }
            }
        }.runTaskTimer(PixelBedwars.getPixelBedwars(), 0L, 20L);
        this.gameTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (getGameState() == GameState.WAITING) {
                    BedwarsGame.this.getGamePlayers().stream()
                            .map(GamePlayer::getPlayer)
                            .forEach(player -> {
                                sendActionbar(player, language.getWaitActionbar());
                            });
                    return;
                }
                if (getGameState() == GameState.END) {
                    BedwarsGame.this.getGamePlayers().stream()
                            .map(GamePlayer::getPlayer)
                            .forEach(player -> {
                                sendActionbar(player, language.getEndActionbar());
                            });
                    return;
                }
                BedwarsGame.this.getGamePlayers().stream()
                        .map(GamePlayer::getPlayer)
                        .forEach(player -> {
                            if (isPlayerInvisible(player)) {
                                getGamePlayers().forEach(gamePlayer -> {
                                    gamePlayer.getPlayer().hidePlayer(player);
                                });
                            } else {
                                getGamePlayers().forEach(gamePlayer -> {
                                    if (!gamePlayer.getPlayer().canSee(player)) {
                                        gamePlayer.getPlayer().showPlayer(player);
                                    }
                                });
                            }
                            sendActionbar(player, language.getGamingActionbar());
                        });
                getTeamList().stream()
                        .map(GameTeam::getTeamMeta)
                        .forEach(TeamMeta::tickSpawner);
                updateScoreboard();
            }
        }.runTaskTimer(PixelBedwars.getPixelBedwars(), 0L, 10L);
        this.healthRegenTask = new BukkitRunnable() {
            @Override
            public void run() {
                getTeamList()
                        .forEach(gameTeam -> {
                            if (gameTeam.getTeamShoppingProperties().healthRegenEnable) {
                                gameTeam.getAlivePlayers()
                                        .stream()
                                        .map(GamePlayer::getPlayer)
                                        .forEach(player -> {
                                            if (player.getLocation().distance(gameTeam.getTeamMeta().getTeamGameLocation().toBukkitLocation()) <= 8) {
                                                if (player.getHealth() != player.getMaxHealth()) {
                                                    player.setHealth(player.getHealth() + 1);
                                                }
                                            }
                                        });
                            }
                        });
            }
        }.runTaskTimer(PixelBedwars.getPixelBedwars(), 0L, 40L);

        Bukkit.getPluginManager().registerEvents(this, PixelBedwars.getPixelBedwars());
        this.gameState = GameState.WAITING;
        return this;
    }

    public void doShopInit() {
        getGameSetting().getPlayerShopEntityList().forEach(vecLoc3D -> {
            Location location1 = vecLoc3D.toBukkitLocation();
            checkChunk(location1);
            EntityType entityType = PixelBedwars.getPixelBedwars().getPlayerShop().getEntityType();
            Entity entity = location1.getWorld().spawnEntity(location1, entityType);
            Hologram hologram = HologramsAPI.createHologram(PixelBedwars.getPixelBedwars(), location1.add(0, 3, 0));
            getLanguage().getPlayerShopHologramTexts().forEach(hologram::appendTextLine);
            NMSUtils.clearEntityAI(entity);
            setEntityInvulnerability(entity, true);
            net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
            if (handle instanceof EntityInsentient) {
                NMSUtils.addAIToEntity(entity, new PathfinderGoalLookAtPlayer(((EntityInsentient) handle), EntityPlayer.class, 8));
            }
        });
        getGameSetting().getTeamShopEntityList().forEach(vecLoc3D -> {
            Location location1 = vecLoc3D.toBukkitLocation();
            checkChunk(location1);
            EntityType teamGuiEntityType = getGameSetting().getTeamGuiEntityType();
            Entity entity = location1.getWorld().spawnEntity(location1, teamGuiEntityType);
            Hologram hologram = HologramsAPI.createHologram(PixelBedwars.getPixelBedwars(), location1.add(0, 3, 0));
            getLanguage().getTeamShopHologramTexts().forEach(hologram::appendTextLine);
            NMSUtils.clearEntityAI(entity);
            setEntityInvulnerability(entity, true);
            net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
            if (handle instanceof EntityInsentient) {
                NMSUtils.addAIToEntity(entity, new PathfinderGoalLookAtPlayer(((EntityInsentient) handle), EntityPlayer.class, 8));
            }
        });
    }

    public StageEntry getNextStage() {
        return getGameSetting().getStageEntryList()
                .stream()
                .sorted(Comparator.comparingInt(StageEntry::getFlow))
                .filter(se -> se.getCounter() >= 0)
                .findFirst().orElse(getGameSetting().getStageEntryList().get(0));
    }

    private void sendActionbar(Player player, String s) {
        try {
            s = PlaceholderAPI.setPlaceholders(player, s);
        } catch (Exception e) {
            //ignored
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + s + "\"}"), (byte) 2));
    }

    public void sendTitle(Player player, String string) {
        player.sendTitle(string, "");
    }

    public void broadcastTitle(String string) {
        this.getGamePlayers().stream()
                .map(GamePlayer::getPlayer)
                .forEach(player -> sendTitle(player, string));
    }

    private void clearPlayerInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack != null) {
                if (itemStack.hasItemMeta()) {
                    if (itemStack.getItemMeta().hasLore()) {
                        if (itemStack.getItemMeta().getLore().stream().noneMatch(s -> s.contains("无法掉落"))) {
                            player.getInventory().setItem(i, new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        }
    }

    public void endGame() {
        if (gameState != GameState.GAMING) {
            return;
        }
        this.gameState = GameState.END;
        broadcastMessage(language.getGameEndMessage(), null);
        calWinner();
        calStatistic();
        this.getGamePlayers().forEach(gamePlayer -> {
            gamePlayer.getPlayer().setGameMode(GameMode.SPECTATOR);
            gamePlayer.getPlayer().teleport(gameSetting.getPlayerRespawnWaitLocation().toBukkitLocation());
        });
        this.getGamePlayers().stream()
                .map(GamePlayer::getPlayer)
                .forEach(this::sendToHub);
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.spigot().restart();
            }
        }.runTaskLater(PixelBedwars.getPixelBedwars(), gameSetting.getServerRestartDelay() * 20L);
    }

    public void calStatistic() {
        PixelBedwars pixelBedwars = PixelBedwars.getPixelBedwars();
        DatabaseManager databaseManagerBase = pixelBedwars.getDatabaseManagerBase();
        gamePlayers.forEach(gamePlayer -> {
            pixelBedwars.setPlayerStatistic(() -> {
                PlayerStatisticModel playerStatistic = pixelBedwars.getPlayerStatistic(gamePlayer.getPlayer());
                playerStatistic.setKills(playerStatistic.getKills() + gamePlayer.getKills());
                playerStatistic.setDeath(playerStatistic.getDeath() + gamePlayer.getDeath());
                playerStatistic.setFinalKills(playerStatistic.getFinalKills() + gamePlayer.getFinalKills());
                playerStatistic.setBedDestroy(playerStatistic.getFinalKills() + gamePlayer.getKills());
                return playerStatistic;
            });
        });
    }

    public void calWinner() {
        try {
            GameTeam gameTeam = getTeamList().stream()
                    .max(Comparator.comparingInt(o -> o.getTeam().getEntries()
                            .stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .map(this::getBedwarsPlayer)
                            .filter(Objects::nonNull)
                            .mapToInt(GamePlayer::getKills)
                            .sum())).orElseThrow(IllegalAccessException::new);
            PixelBedwars pixelBedwars = PixelBedwars.getPixelBedwars();
            DatabaseManager databaseManagerBase = pixelBedwars.getDatabaseManagerBase();
            List<Player> collect = gameTeam.getTeam().getEntries()
                    .stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            collect
                    .forEach(player -> {
                        pixelBedwars.setPlayerStatistic(() -> {
                            PlayerStatisticModel playerStatistic = pixelBedwars.getPlayerStatistic(player);
                            playerStatistic.setWin(playerStatistic.getWin() + 1);
                            return playerStatistic;
                        });
                    });
            gamePlayers.stream()
                    .filter(gamePlayer -> !collect.contains(gamePlayer))
                    .forEach(gamePlayer -> {
                        pixelBedwars.setPlayerStatistic(() -> {
                            PlayerStatisticModel playerStatistic = pixelBedwars.getPlayerStatistic(gamePlayer.getPlayer());
                            playerStatistic.setFail(playerStatistic.getFail() + 1);
                            return playerStatistic;
                        });
                    });
            broadcastMessage(language.getTeamWinMessage(), ImmutableMap.of("%team%", gameTeam.getTeamMeta().getTeamName()));
        } catch (IllegalAccessException e) {
            Bukkit.broadcastMessage("无法计算胜利队伍,平局!");
        }
        broadcastMessage(language.getServerRestartMessage(), ImmutableMap.of("%sec%", String.valueOf(gameSetting.getServerRestartDelay())));
    }

    private GameTeam autoPlayerTeam() {
        return teamList.stream()
                .min(Comparator.comparingInt(o -> o.getTeam().getSize()))
                .orElse(teamList.get(0));
    }

    public GameTeam getPlayerTeam(Player player) {
        return getTeamList().stream()
                .filter(gameTeam -> gameTeam.hasPlayer(player))
                .findAny()
                .orElse(null);
    }

    public void playerLeave(Player player) {
        broadcastMessage(language.getPlayerQuitMessage(), ImmutableMap.of("%player%", player.getDisplayName()));
        gamePlayers.remove(getBedwarsPlayer(player));
        GameTeam playerTeam = getPlayerTeam(player);
        if (playerTeam == null) {
            return;
        }
        playerTeam.removePlayer(player);
        resetPlayer(player);
    }

    public void resetPlayer(Player player) {
        player.setMaxHealth(20);
        player.getInventory().clear();
        player.setLevel(0);
        player.setTotalExperience(0);
    }

    public Location playerRespawn(Player player) {
        return getPlayerTeam(player).getTeamMeta().getTeamGameLocation().toBukkitLocation();
    }

    public boolean playerDeath(Player player) {
        GamePlayer gamePlayer = getBedwarsPlayer(player);
        if (gamePlayer == null) {
            return true;
        }
        if (!canPlayerRespawn(gamePlayer)) {
            gamePlayer.setTotallyDeath(true);
        }
        gamePlayer.addDeath();
        return gamePlayer.isTotallyDeath();
    }

    public void startGame() {
        if (gameState != GameState.WAITING) {
            return;
        }
        Location borderCenterLocation = gameSetting.getPlayerRespawnWaitLocation().toBukkitLocation();
        checkChunk(borderCenterLocation);
        this.getGamePlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (getPlayerTeam(gamePlayer.getPlayer()) == null) {
                autoPlayerTeam().addPlayer(player);
            }
            resetPlayer(player);
            Location location = getPlayerTeam(player).getTeamMeta().getTeamGameLocation().toBukkitLocation();
            checkChunk(location);
            player.teleport(location);
            setEntityInvulnerability(player, false);
            clearPlayerInventory(player);
            player.setMaxHealth(gameSetting.getPlayerMaxHealth());
            player.setHealth(player.getMaxHealth());
            addDefaultWeapon(player);
        });
        broadcastMessage(language.getGameStart(), null);
        doShopInit();
        this.gameState = GameState.GAMING;
    }

    public boolean isTeamDead(GameTeam gameTeam) {
        return gameTeam.getAlivePlayers().size() == 0;
    }

    public boolean hasPlayer(Player player) {
        return gamePlayers.stream()
                .anyMatch(gamePlayer -> gamePlayer.isPlayerEqual(player));
    }

    public void checkChunk(Location location) {
        Chunk chunk = location.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }
    }

    public void setEntityInvulnerability(Entity player, boolean enable) {
        if (enable) {
            player.setMetadata("Invulnerability", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), true));
        } else {
            player.removeMetadata("Invulnerability", PixelBedwars.getPixelBedwars());
        }
    }

    public boolean isInvulnerability(Entity player) {
        return player.hasMetadata("Invulnerability");
    }

    public GamePlayer getBedwarsPlayer(Player player) {
        return this.getGamePlayers().stream()
                .filter(gamePlayer -> gamePlayer.isPlayerEqual(player))
                .findAny()
                .orElse(null);
    }

    public void playerJoin(Player player) {
        if (gameState != GameState.WAITING) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlying(true);
        }
        if (hasPlayer(player)) {
            return;
        }
        if (this.getGamePlayers().size() + 1 > gameSetting.getMaxPlayer()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlying(true);
            return;
        }
        if (this.getGamePlayers().size() + 1 > gameSetting.getTeamMetaList()
                .stream()
                .mapToInt(TeamMeta::getMaxPlayer)
                .sum()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setFlying(true);
            return;
        }
        PixelBedwars.getPixelBedwars().getCorpsesManager()
                .registerPacketListener(player);
        resetPlayer(player);
        gamePlayers.add(new GamePlayer(player));
        player.teleport(gameSetting.getPlayerWaitLocation().toBukkitLocation());
        setEntityInvulnerability(player, true);

        player.getInventory().setItem(gameSetting.getSelectTeamItemSlot(), new ItemFactory(() -> new ItemStack(Material.valueOf(gameSetting.getSelectTeamItemType()))).setDisplayName(language.getSlimeBallName()).setLore(language.getSlimeBallLore()).pack());
        player.getInventory().setItem(gameSetting.getQuitItemSlot(), new ItemFactory(() -> new ItemStack(Material.valueOf(gameSetting.getQuitItemType()))).setDisplayName(language.getQuitItemName()).setLore(language.getQuitItemLore()).pack());
        if (this.getGamePlayers().size() == gameSetting.getMaxPlayer()) {
            gameStartCounter = gameSetting.getPlayerFullWaitTime();
        }
        autoPlayerTeam().addPlayer(player);
        broadcastMessage(language.getPlayerJoinMessage(), ImmutableMap.of("%player%", player.getDisplayName()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerClickInventory(InventoryClickEvent inventoryClickEvent) {
        Player whoClicked = (Player) inventoryClickEvent.getWhoClicked();
        if (gameState == GameState.WAITING) {
            inventoryClickEvent.setCancelled(true);
        }
    }

    private boolean canPlayerRespawn(GamePlayer player) {
        return !getPlayerTeam(player.getPlayer()).isBedDestroyed();
    }

    private void markBlockBreakable(Block block) {
        block.setMetadata("human", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), true));
    }

    private boolean isBlockBreakable(Player player, Block block) {
        boolean human = isBlockPlacedByHuman(block) || block.getType() == Material.BED_BLOCK;
        return human && canBreakBed(player, block);
    }

    private boolean isBlockPlacedByHuman(Block block) {
        return block.hasMetadata("human");
    }

    public GameTeam getGameTeamByBedLocation(Block block) {
        return getTeamList().stream()
                .filter(gameTeam -> checkTeamBedLocation(block, gameTeam))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private boolean canBreakBed(Player player, Block block) {
        //如果破坏的方块周围有任何方块是自己队伍的,阻止掉
        GameTeam playerTeam = getPlayerTeam(player);
        return checkTeamBedLocation(block, playerTeam);
    }

    public void playerDestroyBed(Player player, GameTeam gameTeam) {
        String playerDestroyBedMessage = getLanguage().getPlayerDestroyBedMessage();
        getBedwarsPlayer(player).addBedDestroyed();
        broadcastMessage(playerDestroyBedMessage, ImmutableMap.of("%player%", player.getName(), "%team%", gameTeam.getTeamMeta().getTeamName()));
    }

    public void playerDestroyBed(GamePlayer player, GameTeam gameTeam) {
        playerDestroyBed(player.getPlayer(), gameTeam);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent entityExplodeEvent) {
        if (((CraftEntity) entityExplodeEvent.getEntity()).getHandle() instanceof BedwarsEnderDragon) {
            return;
        }
        if (entityExplodeEvent.getEntity() instanceof TNTPrimed) {
            entityExplodeEvent.setCancelled(true);
            Location location = entityExplodeEvent.getLocation();
            location.getWorld().createExplosion(location, getGameSetting().getTntExplodePower());
        }
        processExplode(entityExplodeEvent.blockList(), entityExplodeEvent.getLocation());
    }

    @EventHandler
    public void onEntityExplode(BlockExplodeEvent blockExplodeEvent) {
        processExplode(blockExplodeEvent.blockList(), blockExplodeEvent.getBlock().getLocation());
    }

    private void processExplode(List<Block> blocks, Location explosion) {
        for (Block block : new ArrayList<>(blocks)) {
            Location location = block.getLocation();
            if (block.getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
                continue;
            }
            if (!isBlockPlacedByHuman(block)) {
                blocks.remove(block);
                continue;
            }

            if (getEast(location).getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
            }
            if (getWest(location).getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
            }
            if (getNorth(location).getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
            }
            if (getSouth(location).getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
            }
            if (getUp(location).getType() == Material.STAINED_GLASS) {
                blocks.remove(block);
            }
        }
    }

    private Block getEast(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPosition east = blockPosition.east();
        return new Location(location.getWorld(), east.getX(), east.getY(), east.getZ()).getBlock();
    }

    private Block getWest(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPosition east = blockPosition.west();
        return new Location(location.getWorld(), east.getX(), east.getY(), east.getZ()).getBlock();
    }

    private Block getNorth(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPosition east = blockPosition.north();
        return new Location(location.getWorld(), east.getX(), east.getY(), east.getZ()).getBlock();
    }

    private Block getSouth(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPosition east = blockPosition.south();
        return new Location(location.getWorld(), east.getX(), east.getY(), east.getZ()).getBlock();
    }

    private Block getUp(Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ()).getBlock();
    }

    private Block getDown(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockPosition east = blockPosition.down();
        return new Location(location.getWorld(), east.getX(), east.getY(), east.getZ()).getBlock();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        Player player = blockPlaceEvent.getPlayer();
        Block blockPlaced = blockPlaceEvent.getBlockPlaced();
        if (getTeamList().stream()
                .map(GameTeam::getTeamMeta)
                .map(TeamMeta::getTeamGameLocation)
                .map(VecLoc3D::toBukkitLocation)
                .anyMatch(location -> location.distance(blockPlaced.getLocation()) <= 8)) {
            blockPlaceEvent.setCancelled(true);
            return;
        }

        markBlockBreakable(blockPlaceEvent.getBlock());
        if (hasPlayer(player) && gameState != GameState.GAMING) {
            blockPlaceEvent.setCancelled(true);
            return;
        }
        if (blockPlaced.getType() == Material.TNT) {
            Location location = blockPlaced.getLocation();
            TNTPrimed tntPrimed = (TNTPrimed) location.getWorld().spawnEntity(location.add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
            tntPrimed.setFuseTicks(gameSetting.getTntExplodeDelay());
            location.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        if (gameState != GameState.GAMING) {
            blockBreakEvent.setCancelled(true);
            return;
        }
        Block block = blockBreakEvent.getBlock();
        if (!isBlockBreakable(player, block)) {
            blockBreakEvent.setCancelled(true);
        } else {
            if (block.getType() == Material.BED_BLOCK) {
                try {
                    blockBreakEvent.setCancelled(true);
                    block.setType(Material.AIR);
                    GameTeam gameTeamByBedLocation = getGameTeamByBedLocation(block);
                    playerDestroyBed(player, gameTeamByBedLocation);
                } catch (NullPointerException ignored) {

                }
            }
        }
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageEvent entityDamageEvent) {
        if (isInvulnerability(entityDamageEvent.getEntity())) {
            entityDamageEvent.setCancelled(true);
            return;
        }
        if (!(entityDamageEvent.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) entityDamageEvent.getEntity();

        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (hasPlayer(player)) {
                if (gameState == GameState.WAITING) {
                    player.teleport(gameSetting.getPlayerWaitLocation().toBukkitLocation());
                }
                if (gameState == GameState.GAMING) {
                    player.setHealth(0);
                }
            }
        }
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            entityDamageEvent.setDamage(1);
        }
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            if (!(entityDamageByEntityEvent.getDamager() instanceof Player)) {
                return;
            }
            Player damager = (Player) entityDamageByEntityEvent.getDamager();
            GameTeam playerTeam = getPlayerTeam(damager);
            GameTeam playerTeam1 = getPlayerTeam(player);
            if (playerTeam.equals(playerTeam1)) {
                entityDamageByEntityEvent.setCancelled(true);
            }
        }
    }

    public boolean canGameStart() {
        return this.getGamePlayers().size() >= gameSetting.getMinPlayer() && teamList.size() > 1 && teamList.stream().allMatch(gameTeam -> gameTeam.getTeam().getSize() >= gameTeam.getTeamMeta().getMinPlayer());
    }

    public boolean canGameContinue() {
        return getTeamList().stream()
                .filter(gameTeam -> gameTeam.getTeam().getSize() >= 1)
                .collect(Collectors.toList())
                .size() >= 2 && getAliveTeams() >= 2;
    }

    private int getAliveTeams() {
        long count = getTeamList().stream().filter(team -> !isTeamDead(team)).count();
        return (int) count;
    }

    public void updateScoreboard() {
        if (getGameState() == GameState.WAITING) {
            this.getGamePlayers().stream()
                    .map(GamePlayer::getPlayer)
                    .forEach(player -> gameSetting.getWaitScoreboard().show(player));
            return;
        }
        if (getGameState() == GameState.GAMING) {
            this.getGamePlayers().stream()
                    .map(GamePlayer::getPlayer)
                    .forEach(player -> gameSetting.getGamingScoreboard().show(player));
        }
        if (getGameState() == GameState.END) {
            this.getGamePlayers().stream()
                    .map(GamePlayer::getPlayer)
                    .forEach(player -> gameSetting.getEndScoreboard().show(player));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        Player entity = playerDeathEvent.getEntity();
        playerDeathEvent.setKeepInventory(true);
        if (hasPlayer(entity)) {
            playerDeathEvent.setDeathMessage(null);
            GamePlayer gamePlayer = getBedwarsPlayer(entity);
            gamePlayer.addDeath();
            EntityLiving lastDamager = ((CraftPlayer) gamePlayer.getPlayer()).getHandle().lastDamager;
            if (lastDamager != null) {
                if (lastDamager instanceof EntityPlayer) {
                    CraftPlayer bukkitEntity = (CraftPlayer) lastDamager.getBukkitEntity();
                    GamePlayer player = getBedwarsPlayer(bukkitEntity);
                    if (player != null) {
                        if (isTeamDead(getPlayerTeam(entity))) {
                            player.addFinalKills();
                        } else {
                            player.addKill();
                        }
                        broadcastMessage(language.getPlayerKillOthersMessage(), ImmutableMap.of("%killer%", player.getPlayer().getDisplayName(), "%player%", entity.getDisplayName()));
                    }
                }
            } else {
                broadcastMessage(language.getPlayerSuicideMessage(), ImmutableMap.of("%player%", entity.getDisplayName()));
            }


            PixelBedwars pixelBedwars = PixelBedwars.getPixelBedwars();
            if (gameSetting.isPlayerCorpseEnable()) {
                PlayerInventory i1 = entity.getInventory();
                Inventory inventory = Bukkit.createInventory(null, 54);
                inventory.setContents(i1.getContents());
                Corpses.CorpseData corpseData = pixelBedwars.getCorpsesManager()
                        .spawnCorpse(entity, null, entity.getLocation(), inventory, 0);
                this.playerCorpseDataMap.put(entity.getUniqueId(), corpseData);
                Bukkit.getScheduler().runTaskLater(pixelBedwars, () -> {
                    pixelBedwars.getCorpsesManager().removeCorpse(corpseData);
                }, gameSetting.getPlayerCorpseDespawnRate() * 20L);
            }
            if (playerDeath(entity)) {

            }
            Bukkit.getScheduler().runTaskLater(PixelBedwars.getPixelBedwars(), () -> {
                entity.spigot().respawn();
            }, 10L);
        }
    }

    public void onPlayerSuccessRespawn(Player player) {
        resetPlayer(player);
        player.teleport(playerRespawn(player));
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setMaxHealth(gameSetting.getPlayerMaxHealth());
        CorpsesManager corpsesManager = PixelBedwars.getPixelBedwars()
                .getCorpsesManager();
        Corpses.CorpseData corpseData = playerCorpseDataMap.get(player.getUniqueId());
        if (corpseData != null) {
            corpsesManager.removeCorpse(corpseData);
            playerCorpseDataMap.remove(player.getUniqueId());
        }
        getBedwarsPlayer(player).setRespawning(false);
        clearPlayerInventory(player);
        GameTeam playerTeam = getPlayerTeam(player);
        playerTeam.getTeamShoppingProperties().notifyTeamEquipmentChange(playerTeam);
        addDefaultWeapon(player.getPlayer());
    }

    public Location randomLocation(VecLoc3D respawnRegionStartLocation, VecLoc3D respawnRegionEndLocation) {
        double minX;
        double minY;
        double minZ;
        double maxX;
        double maxY;
        double maxZ;
        if (respawnRegionStartLocation.getX() >= respawnRegionEndLocation.getX()) {
            maxX = respawnRegionStartLocation.getX();
            minX = respawnRegionEndLocation.getX();
        } else {
            maxX = respawnRegionEndLocation.getX();
            minX = respawnRegionStartLocation.getX();
        }
        if (respawnRegionStartLocation.getY() >= respawnRegionEndLocation.getY()) {
            maxY = respawnRegionStartLocation.getY();
            minY = respawnRegionEndLocation.getY();
        } else {
            maxY = respawnRegionEndLocation.getY();
            minY = respawnRegionStartLocation.getY();
        }
        if (respawnRegionStartLocation.getZ() >= respawnRegionEndLocation.getZ()) {
            maxZ = respawnRegionStartLocation.getZ();
            minZ = respawnRegionEndLocation.getZ();
        } else {
            maxZ = respawnRegionEndLocation.getZ();
            minZ = respawnRegionStartLocation.getZ();
        }
        double randomX = getRandomNumberInRange(minX, maxX);
        double randomZ = getRandomNumberInRange(minZ, maxZ);
        return getEmptyY(new Location(Bukkit.getWorld(respawnRegionEndLocation.getWorld()), randomX, minY, randomZ));
    }

    public double getRandomNumberInRange(double min, double max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((int) ((max - min) + 1)) + min;
    }

    public Location getEmptyY(Location location) {
        for (int i = location.getBlockY(); i < 255; i++) {
            location.setY(i);
            if (location.getBlock().getType() == Material.AIR) {
                return location;
            }
        }
        return location;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        playerJoinEvent.setJoinMessage(null);
        playerJoin(playerJoinEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        playerQuitEvent.setQuitMessage(null);
        playerLeave(playerQuitEvent.getPlayer());
        if (!canGameContinue()) {
            endGame();
        }
    }

    public void requestSwitchTeamByName(Player player, String teamName) {
        GameTeam teamByName = getTeamByName(teamName);
        if (teamByName == null) {
            return;
        }
        GameTeam playerTeam = getPlayerTeam(player);
        List<GameTeam> collect = getTeamList().stream()
                .filter(gameTeam -> !gameTeam.getTeamMeta().getTeamName().equals(teamName))
                .collect(Collectors.toList());
        GameTeam gameTeam = collect.stream()
                .min(Comparator.comparingInt(team -> team.getTeam().getSize()))
                .orElse(teamList.get(0));
        GamePlayer gamePlayer = getBedwarsPlayer(player);
        if (gamePlayer == null) {
            return;
        }

        if (playerTeam == null) {
            teamByName.addPlayer(player);
            return;
        }
        if (playerTeam.getTeamMeta().getTeamName().equals(teamName)) {
            sendMessage(gamePlayer, language.getCanNotSwitchToCurrent(), null);
            return;
        }
        playerTeam.removePlayer(player);
        teamByName.addPlayer(player);
    }

    public GameTeam getTeamByName(String teamName) {
        return getTeamList().stream()
                .filter(gameTeam -> gameTeam.getTeamMeta().getTeamName().equals(teamName))
                .findAny()
                .orElseThrow(NullPointerException::new);
    }

    public void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
        byteArrayDataOutput.writeUTF("Connect");
        byteArrayDataOutput.writeUTF(serverName);
        player.sendPluginMessage(PixelBedwars.getPixelBedwars(), "BungeeCord", byteArrayDataOutput.toByteArray());
    }

    public void sendToHub(Player player) {
        List<String> hubServers = getGameSetting().getHubServers();
        Collections.shuffle(hubServers);
        sendToServer(player, hubServers.get(0));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        GamePlayer gamePlayer = getBedwarsPlayer(player);
        GameTeam playerTeam = getPlayerTeam(player);
        if (gamePlayer != null) {
            if (!gamePlayer.isTotallyDeath()) {
                gamePlayer.setRespawning(true);
            }
            player.setGameMode(GameMode.SPECTATOR);
            playerRespawnEvent.setRespawnLocation(playerTeam.getTeamMeta().getTeamGameLocation().toBukkitLocation());
            if (!canGameContinue()) {
                endGame();
            }
            new PlayerRespawnTask(gamePlayer).start();
        }
    }

    @EventHandler
    public void onMotdRequest(ServerListPingEvent serverListPingEvent) {
        if (gameState == GameState.WAITING) {
            serverListPingEvent.setMotd(gameSetting.getMotdWait());
            return;
        }
        if (gameState == GameState.GAMING) {
            serverListPingEvent.setMotd(gameSetting.getMotdGaming());
            return;
        }
        if (gameState == GameState.END) {
            serverListPingEvent.setMotd(gameSetting.getMotdEnd());
        }
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent playerPreLoginEvent) {
        if (gameState != GameState.WAITING) {
            playerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "游戏无法中途加入");
        }
    }

    public void sendMessage(Player gamePlayer, String string, @Nullable Map<String, String> map) {
        gamePlayer.sendMessage(formatMessage(PlaceholderAPI.setPlaceholders(gamePlayer, string), map));
    }

    public void sendMessage(GamePlayer gamePlayer, String string, @Nullable Map<String, String> map) {
        sendMessage(gamePlayer.getPlayer(), string, map);
    }

    public void broadcastMessage(String string, @Nullable Map<String, String> map) {
        this.getGamePlayers().forEach(gamePlayer -> {
            sendMessage(gamePlayer, string, map);
        });
    }

    public String formatMessage(String string, @Nullable Map<String, String> map) {
        string = string.replace("&", "§");
        if (map != null) {
            for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                string = string.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }
        return string;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent playerDropItemEvent) {
        if (gameState != GameState.GAMING) {
            playerDropItemEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent foodLevelChangeEvent) {
        foodLevelChangeEvent.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        GamePlayer gamePlayer = getBedwarsPlayer(player);
        if (gamePlayer == null) {
            return;
        }
        if (playerInteractEvent.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) {
            return;
        }
        if (itemInHand.getItemMeta() == null) {
            return;
        }
        if (itemInHand.getItemMeta().getDisplayName() == null) {
            return;
        }
        if (itemInHand.getItemMeta().getDisplayName().equals(language.getQuitItemName())) {
            sendToHub(player);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent weatherChangeEvent) {
        weatherChangeEvent.setCancelled(gameSetting.isDisableWeather());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        asyncPlayerChatEvent.setCancelled(true);
        Bukkit.broadcastMessage(PlaceholderAPI.setPlaceholders(asyncPlayerChatEvent.getPlayer(), language.getPlayerChatFormat().replace("%s", asyncPlayerChatEvent.getMessage())));
    }

    public GameTeam getPlayerTeam(GamePlayer gamePlayer) {
        return getPlayerTeam(gamePlayer.getPlayer());
    }

    public void destroyAllBed() {
        getTeamList().stream()
                .map(GameTeam::getTeamMeta)
                .map(TeamMeta::getTeamBedLocation)
                .map(VecLoc3D::toBukkitLocation)
                .map(Location::getBlock)
                .filter(Objects::nonNull)
                .forEach(block -> block.setType(Material.AIR, true));
    }

    public void callDeathMatch() {
        getTeamList().forEach(gameTeam -> {
            Location location = getGameSetting().getPlayerRespawnWaitLocation().toBukkitLocation();
            WorldServer worldServer = ((CraftWorld) location
                    .getWorld()).getHandle();
            if (gameTeam.getTeamShoppingProperties().isDoubleDragonEnable()) {
                BedwarsEnderDragon e = new BedwarsEnderDragon(worldServer, gameTeam);
                getBedwarsEnderDragons().add(e);
                e.spawnEntity(location);
            }
            BedwarsEnderDragon e = new BedwarsEnderDragon(worldServer, gameTeam);
            getBedwarsEnderDragons().add(e);
            e.spawnEntity(location);
        });
    }

    @EventHandler
    public void onPlayerUsePotion(PlayerItemConsumeEvent playerItemConsumeEvent) {
        ItemStack item = playerItemConsumeEvent.getItem();
        if (item.getType() == Material.POTION && item.getDurability() == 8270) {
            Player player = playerItemConsumeEvent.getPlayer();
            setPlayerInvisible(player, true);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 1, false));
            player.sendMessage("你隐身了!");
            Bukkit.getScheduler().runTaskLater(PixelBedwars.getPixelBedwars(), () -> {
                setPlayerInvisible(player, false);
            }, 30 * 20L);
        }
    }

    @EventHandler
    public void onPlayerDamageByOthers(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity damager = entityDamageByEntityEvent.getDamager();
        Entity victim = entityDamageByEntityEvent.getEntity();
        if (!(victim instanceof Player)) {
            return;
        }
        Player victimPlayer = (Player) victim;
        if (isPlayerInvisible(victimPlayer)) {
            setPlayerInvisible(victimPlayer, false);
        }
    }

    @EventHandler
    public void onPlayerTargetOthers(PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_AIR || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack hand = player.getItemInHand();
            if (hand.getType() == Material.FIREBALL) {
                float fireballCooldown = getFireballCooldown(player);
                System.out.println(fireballCooldown);
                if (fireballCooldown <= 0) {
                    playerInteractEvent.setCancelled(true);
                    player.launchProjectile(Fireball.class, player.getEyeLocation().getDirection());
                    hand.setAmount(hand.getAmount() - 1);
                    player.setItemInHand(hand);
                    setFireballCooldown(player, gameSetting.getFireballCooldown());
                } else {
                    player.sendMessage(formatMessage(getLanguage().getFireballCoolingDownMessage(), ImmutableMap.of("%s", String.valueOf(fireballCooldown))));
                }

            }
            if (hand.getType() == Material.EGG) {
                playerInteractEvent.setCancelled(true);
                BedwarsEgg.shoot(player.getWorld(), player, getPlayerTeam(player).getTeamMeta());
                hand.setAmount(hand.getAmount() - 1);
                player.setItemInHand(hand);
            }

            if (playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK && hand.getType() == Material.MONSTER_EGG && hand.getDurability() == 68) {
                playerInteractEvent.setCancelled(true);
                new BedwarsGolem(((CraftWorld) player.getWorld()).getHandle(), getPlayerTeam(player)).spawnEntity(playerInteractEvent.getClickedBlock().getLocation().add(0, 1, 0));
                hand.setAmount(hand.getAmount() - 1);
                player.setItemInHand(hand);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent projectileHitEvent) {
        if (projectileHitEvent.getEntity().getType() == EntityType.FIREBALL) {
            Location location = projectileHitEvent.getEntity().getLocation();
            projectileHitEvent.getEntity().remove();
            location.getWorld().createExplosion(location, getGameSetting().getFireballExplodePower());
        }
    }

    private void setFireballCooldown(Player player, float cd) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MILLISECOND, (int) (cd * 1000));
        player.removeMetadata("FIREBALL", PixelBedwars.getPixelBedwars());
        player.setMetadata("FIREBALL", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), instance.getTime()));
    }

    private float getFireballCooldown(Player player) {
        if (player.hasMetadata("FIREBALL")) {
            Date fireball = (Date) player.getMetadata("FIREBALL").get(0).value();
            Date now = new Date();
            return (float) (fireball.getTime() - now.getTime()) / 1000;
        }
        return 0;
    }

    private boolean getLookingAt(Player player, Player player1) {
        Location eye = player.getEyeLocation();
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.99D;
    }

    public void setPlayerInvisible(Player player, boolean t) {
        if (t) {
            player.setMetadata("INVISIBLE", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), true));
        } else {
            player.removeMetadata("INVISIBLE", PixelBedwars.getPixelBedwars());
        }
    }

    public boolean isPlayerInvisible(Player player) {
        return player.hasMetadata("INVISIBLE");
    }

}
