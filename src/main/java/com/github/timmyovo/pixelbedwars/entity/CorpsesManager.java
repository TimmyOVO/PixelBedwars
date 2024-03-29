package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Field;
import java.util.*;

public class CorpsesManager extends NmsBase implements Corpses {

    private List<CorpseData> corpses;

    public CorpsesManager() {
        corpses = new ArrayList<CorpseData>();
        Bukkit.getServer().getScheduler()
                .scheduleSyncRepeatingTask(PixelBedwars.getPixelBedwars(), new Runnable() {
                    public void run() {
                        tick();
                    }
                }, 0L, 1L);
    }

    public static DataWatcher clonePlayerDatawatcher(Player player,
                                                     int currentEntId) {
        EntityHuman h = new EntityHuman(
                ((CraftWorld) player.getWorld()).getHandle(),
                ((CraftPlayer) player).getProfile()) {
            public void sendMessage(IChatBaseComponent arg0) {
                return;
            }

            public boolean a(int arg0, String arg1) {
                return false;
            }

            public BlockPosition getChunkCoordinates() {
                return null;
            }

            public boolean isSpectator() {
                return false;
            }
        };
        h.d(currentEntId);
        return h.getDataWatcher();
    }

    public static DataWatcher clonePlayerDatawatcher(GameProfile gp, World world,
                                                     int currentEntId) {
        EntityHuman h = new EntityHuman(
                ((CraftWorld) world).getHandle(),
                gp) {
            public void sendMessage(IChatBaseComponent arg0) {
                return;
            }

            public boolean a(int arg0, String arg1) {
                return false;
            }

            public BlockPosition getChunkCoordinates() {
                return null;
            }

            public boolean isSpectator() {
                return false;
            }
        };
        h.d(currentEntId);
        return h.getDataWatcher();
    }

    public GameProfile cloneProfileWithRandomUUID(GameProfile oldProf,
                                                  String name) {
        GameProfile newProf = new GameProfile(UUID.randomUUID(), name);
        newProf.getProperties().putAll(oldProf.getProperties());
        return newProf;
    }

    public Location getNonClippableBlockUnderPlayer(Location loc, int addToYPos) {
        if (loc.getBlockY() < 0) {
            return null;
        }
        for (int y = loc.getBlockY(); y >= 0; y--) {
            Material m = loc.getWorld()
                    .getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType();
            if (m.isSolid()) {
                return new Location(loc.getWorld(), loc.getX(), y + addToYPos,
                        loc.getZ());
            }
        }
        return null;
    }

    public CorpseData spawnCorpse(Player p, String overrideUsername, Location loc, Inventory inv, int facing) {
        int entityId = getNextEntityId();
        GameProfile prof = cloneProfileWithRandomUUID(
                ((CraftPlayer) p).getProfile(), p.getName());
        DataWatcher dw = clonePlayerDatawatcher(p, entityId);
        //dw.watch(10, ((CraftPlayer) p).getHandle().getDataWatcher().getByte(10));
        Location locUnder = getNonClippableBlockUnderPlayer(loc, 1);
        Location used = locUnder != null ? locUnder : loc;
        used.setYaw(loc.getYaw());
        used.setPitch(loc.getPitch());
        NMSCorpseData data = new NMSCorpseData(prof, used, dw, entityId,
                600 * 20, inv, facing);

        if (p.getKiller() != null) {
            data.killerName = p.getKiller().getName();
            data.killerUUID = p.getKiller().getUniqueId();
        }

        data.corpseName = p.getName();
        corpses.add(data);
        return data;
    }

    @Override
    public CorpseData loadCorpse(String gpName, String gpJSON, Location loc, Inventory items, int facing) {
        int entityId = getNextEntityId();
        GameProfile gp = new GameProfile(UUID.randomUUID(), gpName);

        if (gpJSON != null) {
            JsonElement element = new JsonParser().parse(gpJSON);
            PropertyMap propertyMap = new PropertyMap.Serializer().deserialize(element, null, null);
            gp.getProperties().putAll(propertyMap);
        }

        DataWatcher dw = clonePlayerDatawatcher(gp, loc.getWorld(), entityId);

        Location locUnder = getNonClippableBlockUnderPlayer(loc, 1);
        Location used = locUnder != null ? locUnder : loc;
        used.setYaw(loc.getYaw());
        used.setPitch(loc.getPitch());

        NMSCorpseData data = new NMSCorpseData(gp, used, dw, entityId,
                600 * 20, items, facing);

        data.corpseName = gpName;
        corpses.add(data);
        return data;
    }

    public void removeCorpse(CorpseData data) {
        corpses.remove(data);
        data.destroyCorpseFromEveryone();
        if (data.getLootInventory() != null) {
            data.getLootInventory().clear();
            List<HumanEntity> close = new ArrayList<HumanEntity>(data
                    .getLootInventory().getViewers());
            for (HumanEntity p : close) {
                p.closeInventory();
            }
        }
    }

    public int getNextEntityId() {
        try {
            Field entityCount = Entity.class.getDeclaredField("entityCount");
            entityCount.setAccessible(true);
            int id = entityCount.getInt(null);
            entityCount.setInt(null, id + 1);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return (int) Math.round(Math.random() * Integer.MAX_VALUE * 0.25);
        }
    }

    public void tick() {
        List<CorpseData> toRemoveCorpses = new ArrayList<CorpseData>();
        for (CorpseData data : corpses) {
            List<Player> worldPlayers = data.getOrigLocation().getWorld()
                    .getPlayers();
            for (Player p : worldPlayers) {
                if (data.isTickingPlayer(p)) {
                    int ticks = data.getPlayerTicksLeft(p);
                    if (ticks > 0) {
                        data.tickPlayerLater(ticks - 1, p);
                        continue;
                    } else {
                        data.stopTickingPlayer(p);
                    }
                }
                if (data.mapContainsPlayer(p)) {
                    if (isInViewDistance(p, data) && !data.canSee(p)) {
                        data.resendCorpseToPlayer(p);
                        data.setCanSee(p, true);
                    } else if (!isInViewDistance(p, data) && data.canSee(p)) {
                        data.destroyCorpseFromPlayer(p);
                        data.setCanSee(p, false);
                    }
                } else if (isInViewDistance(p, data)) {
                    data.resendCorpseToPlayer(p);
                    data.setCanSee(p, true);
                } else {
                    data.setCanSee(p, false);
                }
            }
            if (data.getTicksLeft() >= 0) {
                if (data.getTicksLeft() == 0) {
                    toRemoveCorpses.add(data);
                } else {
                    data.setTicksLeft(data.getTicksLeft() - 1);
                }
            }
            List<Player> toRemove = new ArrayList<Player>();
            for (Player pl : data.getPlayersWhoSee()) {
                if (!worldPlayers.contains(pl)) {
                    toRemove.add(pl);
                }
            }
            data.removeAllFromMap(toRemove);
            toRemove.clear();
            Set<Player> set = data.getPlayersTicked();
            for (Player pl : set) {
                if (!worldPlayers.contains(pl)) {
                    toRemove.add(pl);
                }
            }
            set.removeAll(toRemove);
            toRemove.clear();
        }
        for (CorpseData data : toRemoveCorpses) {
            removeCorpse(data);
        }
    }

    public List<CorpseData> getAllCorpses() {
        return corpses;
    }

    public void registerPacketListener(Player p) {
        PacketInListener.registerListener(p);
    }

    @Override
    protected void addNbtTagsToSlime(LivingEntity slime) {
        Entity entity = ((CraftEntity) slime).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        entity.c(tag);
        tag.setInt("Silent", 1);
        tag.setInt("Invulnerable", 1);
        tag.setInt("NoAI", 1);
        tag.setInt("NoGravity", 1);
        entity.f(tag);
    }

    public class NMSCorpseData implements CorpseData {

        public String corpseName;
        private Map<Player, Boolean> canSee;
        private Map<Player, Integer> tickLater;
        private GameProfile prof;
        private Location loc;
        private DataWatcher metadata;
        private int entityId;
        private int ticksLeft;
        private Inventory items;
        private InventoryView iv;
        private int slot;
        private int rotation;

        private String killerName;
        private UUID killerUUID;

        public NMSCorpseData(GameProfile prof, Location loc,
                             DataWatcher metadata, int entityId, int ticksLeft,
                             Inventory items, int rotation) {
            this.prof = prof;
            this.loc = loc;
            this.metadata = metadata;
            this.entityId = entityId;
            this.ticksLeft = ticksLeft;
            this.canSee = new HashMap<Player, Boolean>();
            this.tickLater = new HashMap<Player, Integer>();
            this.items = items;
            this.rotation = rotation;
            if (rotation > 3 || rotation < 0) {
                this.rotation = 0;
            }
        }


        @Override
        public int getRotation() {
            return rotation;
        }

        public ItemStack convertBukkitToMc(org.bukkit.inventory.ItemStack stack) {
            return CraftItemStack.asNMSCopy(stack);
			/*if(stack == null){
				return new ItemStack(Item.getById(0));
			}
			ItemStack temp = new ItemStack(Item.getById(stack.getTypeId()), stack.getAmount());
			temp.setData((int)stack.getData().getData());
			if(stack.getEnchantments().size() >= 1) {
				temp.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);//Dummy enchantment
			}
			return temp;*/
        }

        public void setCanSee(Player p, boolean canSee) {
            this.canSee.put(p, Boolean.valueOf(canSee));
        }

        public boolean canSee(Player p) {
            return canSee.get(p).booleanValue();
        }

        public void removeFromMap(Player p) {
            canSee.remove(p);
        }

        public boolean mapContainsPlayer(Player p) {
            return canSee.containsKey(p);
        }

        public Set<Player> getPlayersWhoSee() {
            return canSee.keySet();
        }

        public void removeAllFromMap(Collection<Player> players) {
            canSee.keySet().removeAll(players);
        }

        public int getTicksLeft() {
            return ticksLeft;
        }

        public void setTicksLeft(int ticksLeft) {
            this.ticksLeft = ticksLeft;
        }

        public PacketPlayOutNamedEntitySpawn getSpawnPacket() {
            PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.set(packet, entityId);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet, prof.getId());
                Field c = packet.getClass().getDeclaredField("c");
                c.setAccessible(true);
                c.setInt(packet, MathHelper.floor(loc.getX() * 32.0D));
                Field d = packet.getClass().getDeclaredField("d");
                d.setAccessible(true);
                d.setInt(packet, MathHelper.floor((loc.getY() + 2) * 32.0D));
                Field e = packet.getClass().getDeclaredField("e");
                e.setAccessible(true);
                e.setInt(packet, MathHelper.floor(loc.getZ() * 32.0D));
                Field f = packet.getClass().getDeclaredField("f");
                f.setAccessible(true);
                f.setByte(packet, (byte) (int) (loc.getYaw() * 256.0F / 360.0F));
                Field g = packet.getClass().getDeclaredField("g");
                g.setAccessible(true);
                g.setByte(packet,
                        (byte) (int) (loc.getPitch() * 256.0F / 360.0F));
                Field i = packet.getClass().getDeclaredField("i");
                i.setAccessible(true);
                i.set(packet, metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }

        public PacketPlayOutBed getBedPacket() {
            PacketPlayOutBed packet = new PacketPlayOutBed();
            try {
                Field a = packet.getClass().getDeclaredField("a");
                a.setAccessible(true);
                a.setInt(packet, entityId);
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                b.set(packet,
                        new BlockPosition(loc.getBlockX(), bedLocation(),
                                loc.getBlockZ()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }

        public PacketPlayOutRelEntityMove getMovePacket() {
            PacketPlayOutRelEntityMove packet = new PacketPlayOutRelEntityMove(
                    entityId, (byte) 0, (byte) (-60.8), (byte) 0, false);
            return packet;
        }

        public PacketPlayOutPlayerInfo getInfoPacket() {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    EnumPlayerInfoAction.ADD_PLAYER);
            try {
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<PlayerInfoData> data = (List<PlayerInfoData>) b
                        .get(packet);
                data.add(packet.new PlayerInfoData(prof, 0,
                        EnumGamemode.SURVIVAL, new ChatMessage("")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }

        public PacketPlayOutPlayerInfo getRemoveInfoPacket() {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    EnumPlayerInfoAction.REMOVE_PLAYER);
            try {
                Field b = packet.getClass().getDeclaredField("b");
                b.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<PlayerInfoData> data = (List<PlayerInfoData>) b
                        .get(packet);
                data.add(packet.new PlayerInfoData(prof, 0,
                        EnumGamemode.SURVIVAL, new ChatMessage("")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }

        public Location getTrueLocation() {
            return loc.clone().add(0, 0.1, 0);
        }

        public PacketPlayOutEntityEquipment getEquipmentPacket(int slot, ItemStack stack) {
            return new PacketPlayOutEntityEquipment(entityId, slot, stack);
        }

        @SuppressWarnings("deprecation")
        public void resendCorpseToEveryone() {
            PacketPlayOutNamedEntitySpawn spawnPacket = getSpawnPacket();
            PacketPlayOutBed bedPacket = getBedPacket();
            PacketPlayOutRelEntityMove movePacket = getMovePacket();
            PacketPlayOutPlayerInfo infoPacket = getInfoPacket();
            final PacketPlayOutPlayerInfo removeInfo = getRemoveInfoPacket();
            final PacketPlayOutEntityEquipment helmetInfo = getEquipmentPacket(4, convertBukkitToMc(items.getItem(1)));
            final PacketPlayOutEntityEquipment chestplateInfo = getEquipmentPacket(3, convertBukkitToMc(items.getItem(2)));
            final PacketPlayOutEntityEquipment leggingsInfo = getEquipmentPacket(2, convertBukkitToMc(items.getItem(3)));
            final PacketPlayOutEntityEquipment bootsInfo = getEquipmentPacket(1, convertBukkitToMc(items.getItem(4)));
            final PacketPlayOutEntityEquipment mainhandInfo = getEquipmentPacket(0, convertBukkitToMc(items.getItem(slot + 45)));
            final List<Player> toSend = loc.getWorld().getPlayers();
            for (Player p : toSend) {
                PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;
                p.sendBlockChange(bedLocation(loc),
                        Material.BED_BLOCK, (byte) rotation);
                conn.sendPacket(infoPacket);
                conn.sendPacket(spawnPacket);
                conn.sendPacket(bedPacket);
                conn.sendPacket(movePacket);
                conn.sendPacket(helmetInfo);
                conn.sendPacket(chestplateInfo);
                conn.sendPacket(leggingsInfo);
                conn.sendPacket(bootsInfo);
                conn.sendPacket(mainhandInfo);
            }
            Bukkit.getServer().getScheduler()
                    .scheduleSyncDelayedTask(PixelBedwars.getPixelBedwars(), () -> {
                        for (Player p : toSend) {
                            ((CraftPlayer) p).getHandle().playerConnection
                                    .sendPacket(removeInfo);
                        }
                    }, 20L);
        }

        public Location bedLocation(Location loc) {
            Location l = loc.clone();
            l.setY(bedLocation());
            return l;
        }

        public int bedLocation() {
            return 1;
        }

        @SuppressWarnings("deprecation")
        public void resendCorpseToPlayer(final Player p) {
            PacketPlayOutNamedEntitySpawn spawnPacket = getSpawnPacket();
            PacketPlayOutBed bedPacket = getBedPacket();
            PacketPlayOutRelEntityMove movePacket = getMovePacket();
            PacketPlayOutPlayerInfo infoPacket = getInfoPacket();
            final PacketPlayOutPlayerInfo removeInfo = getRemoveInfoPacket();
            final PacketPlayOutEntityEquipment helmetInfo = getEquipmentPacket(4, convertBukkitToMc(items.getItem(1)));
            final PacketPlayOutEntityEquipment chestplateInfo = getEquipmentPacket(3, convertBukkitToMc(items.getItem(2)));
            final PacketPlayOutEntityEquipment leggingsInfo = getEquipmentPacket(2, convertBukkitToMc(items.getItem(3)));
            final PacketPlayOutEntityEquipment bootsInfo = getEquipmentPacket(1, convertBukkitToMc(items.getItem(4)));
            PlayerConnection conn = ((CraftPlayer) p).getHandle().playerConnection;
            p.sendBlockChange(bedLocation(loc),
                    Material.BED_BLOCK, (byte) rotation);
            conn.sendPacket(infoPacket);
            conn.sendPacket(spawnPacket);
            conn.sendPacket(bedPacket);
            conn.sendPacket(movePacket);
            if (items.getItem(1) != null) {
                conn.sendPacket(helmetInfo);
            }
            if (items.getItem(2) != null) {
                conn.sendPacket(chestplateInfo);
            }
            if (items.getItem(3) != null) {
                conn.sendPacket(leggingsInfo);
            }
            if (items.getItem(4) != null) {
                conn.sendPacket(bootsInfo);
            }
            Bukkit.getServer().getScheduler()
                    .scheduleSyncDelayedTask(PixelBedwars.getPixelBedwars(), () -> ((CraftPlayer) p).getHandle().playerConnection
                            .sendPacket(removeInfo), 20L);

        }

        @SuppressWarnings("deprecation")
        public void destroyCorpseFromPlayer(Player p) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(
                    entityId);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            Block b = bedLocation(loc).getBlock();
            boolean removeBed = true;
            for (CorpseData cd : getAllCorpses()) {
                if (cd != this
                        && bedLocation(cd.getOrigLocation())
                        .getBlock().getLocation()
                        .equals(b.getLocation())) {
                    removeBed = false;
                    break;
                }
            }
            if (removeBed) {
                p.sendBlockChange(b.getLocation(), b.getType(), b.getData());
            }
        }

        public Location getOrigLocation() {
            return loc;
        }

        @SuppressWarnings("deprecation")
        public void destroyCorpseFromEveryone() {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(
                    entityId);
            Block b = bedLocation(loc).getBlock();
            boolean removeBed = true;
            for (CorpseData cd : getAllCorpses()) {
                if (cd != this
                        && bedLocation(cd.getOrigLocation())
                        .getBlock().getLocation()
                        .equals(b.getLocation())) {
                    removeBed = false;
                    break;
                }
            }
            for (Player p : loc.getWorld().getPlayers()) {
                ((CraftPlayer) p).getHandle().playerConnection
                        .sendPacket(packet);
                if (removeBed) {
                    p.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                }
            }
        }

        public void tickPlayerLater(int ticks, Player p) {
            tickLater.put(p, Integer.valueOf(ticks));
        }

        public int getPlayerTicksLeft(Player p) {
            return tickLater.get(p);
        }

        public void stopTickingPlayer(Player p) {
            tickLater.remove(p);
        }

        public boolean isTickingPlayer(Player p) {
            return tickLater.containsKey(p);
        }

        public Set<Player> getPlayersTicked() {
            return tickLater.keySet();
        }

        public Inventory getItemsInventory() {
            return items;
        }

        public int getEntityId() {
            return entityId;
        }

        public Inventory getLootInventory() {
            return items;
        }

        @Override
        public InventoryView getInventoryView() {
            return iv;
        }

        @Override
        public void setInventoryView(InventoryView iv) {
            this.iv = iv;
        }

        @Override
        public int getSelectedSlot() {
            return slot;
        }

        @Override
        public CorpseData setSelectedSlot(int slot) {
            this.slot = slot;
            return this;
        }


        @Override
        public String getCorpseName() {
            return corpseName;
        }


        @Override
        public String getKillerUsername() {
            return killerName;
        }


        @Override
        public UUID getKillerUUID() {
            return killerUUID;
        }


        @Override
        public String getProfilePropertiesJson() {
            PropertyMap pmap = prof.getProperties();
            JsonElement element = new PropertyMap.Serializer().serialize(pmap, null, null);
            return element.toString();
        }

    }

}