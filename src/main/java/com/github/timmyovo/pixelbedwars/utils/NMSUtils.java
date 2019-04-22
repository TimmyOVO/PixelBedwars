package com.github.timmyovo.pixelbedwars.utils;

import com.google.common.collect.ImmutableMap;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NMSUtils {
    public static void registerEntity(Class clazz, String name, int id) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class entityTypes = Class.forName("net.minecraft.server.v1_8_R3.EntityTypes");
        Field c = entityTypes.getDeclaredField("c");
        Field d = entityTypes.getDeclaredField("d");
        Field f = entityTypes.getDeclaredField("f");
        c.setAccessible(true);
        d.setAccessible(true);
        f.setAccessible(true);
        Map stringToClassMapping = (Map) c.get(null);
        Map classToStringMapping = (Map) d.get(null);
        Map classToIDMapping = (Map) f.get(null);
        stringToClassMapping.put(name, clazz);
        classToStringMapping.put(clazz, name);
        classToIDMapping.put(clazz, Integer.valueOf(id));
    }

    public static void clearEntityAI(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
        if (!(handle instanceof EntityInsentient)) {
            return;
        }
        PathfinderGoalSelector targetSelector = ((EntityInsentient) handle).targetSelector;
        PathfinderGoalSelector goalSelector = ((EntityInsentient) handle).goalSelector;
        Class pathFinderGoalSelector = PathfinderGoalSelector.class;
        Field c = null;
        Field b = null;
        try {
            c = pathFinderGoalSelector.getDeclaredField("b");
            b = pathFinderGoalSelector.getDeclaredField("c");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        c.setAccessible(true);
        b.setAccessible(true);
        List<PathfinderGoalSelector> cList = null;
        List<PathfinderGoalSelector> bList = null;
        List<PathfinderGoalSelector> dList = null;
        List<PathfinderGoalSelector> eList = null;
        try {
            cList = (List<PathfinderGoalSelector>) c.get(targetSelector);
            bList = (List<PathfinderGoalSelector>) b.get(targetSelector);
            dList = (List<PathfinderGoalSelector>) c.get(goalSelector);
            eList = (List<PathfinderGoalSelector>) b.get(goalSelector);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        cList.clear();
        bList.clear();
        dList.clear();
        eList.clear();
    }

    public static void sendPlayerActionbar(Player player, String message, ImmutableMap<String, String> var) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
            message = message.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2));
    }

    public static void sendPlayerJsonMessage(Player player, String message, ImmutableMap<String, String> var) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
            message = message.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message)));
    }

    public static void sendPlayerTitle(Player player, String message, ImmutableMap<String, String> var) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
            message = message.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}")));
    }

    public static void sendPlayerSubtitle(Player player, String message, ImmutableMap<String, String> var) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
            message = message.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}")));
    }

    public static void sendPlayerChat(Player player, String message, ImmutableMap<String, String> var) {
        message = PlaceholderAPI.setPlaceholders(player, message);
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
            message = message.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        player.sendMessage(message);
    }

    public static Optional<Player> getPlayerKiller(Player player) {
        Player killer = player.getKiller();
        if (killer != null) {
            return Optional.of(killer);
        }
        if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent lastDamageCause = (EntityDamageByEntityEvent) player.getLastDamageCause();
            if (lastDamageCause.getDamager() instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) lastDamageCause.getDamager()).getShooter();
                if (shooter instanceof Player) {
                    return Optional.of((Player) shooter);
                }
            }
            if ((lastDamageCause.getDamager() instanceof Player)) {
                return Optional.of(((Player) lastDamageCause.getDamager()));
            }
        }
        return Optional.empty();
    }

    public static String getI18NDisplayName(ItemStack item) {
        if (item == null) {
            return "";
        }
        return CraftItemStack.asNMSCopy(item).getName();
    }

    public static boolean willCauseDeath(EntityDamageEvent entityDamageEvent) {
        Entity entity = entityDamageEvent.getEntity();
        return entity instanceof LivingEntity && !(((LivingEntity) entity).getHealth() - entityDamageEvent.getFinalDamage() > 0);
    }


    public static boolean isNumber(String string) {
        return false;
    }
}
