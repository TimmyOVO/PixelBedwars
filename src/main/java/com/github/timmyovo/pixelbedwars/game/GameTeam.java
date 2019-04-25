package com.github.timmyovo.pixelbedwars.game;

import com.github.skystardust.ultracore.bukkit.modules.item.ItemFactory;
import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.settings.Language;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import com.github.timmyovo.pixelbedwars.shop.TeamShoppingProperties;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode(of = {"teamMeta"})
@AllArgsConstructor
@NoArgsConstructor
public class GameTeam {
    private BedwarsGame bedwarsGame;
    private TeamMeta teamMeta;
    private Team team;
    private TeamShoppingProperties teamShoppingProperties;

    public static void disableItemDrop(ItemStack itemStack) {
        itemStack.setItemMeta(new ItemFactory(() -> itemStack)
                .addLore("无法掉落").getItemMeta());
    }

    public void addPlayer(Player player) {
        Validate.notNull(player);
        if (team.getSize() + 1 > teamMeta.getMaxPlayer()) {
            return;
        }
        PixelBedwars pixelBedwars = PixelBedwars.getPixelBedwars();
        BedwarsGame bedwarsGame = pixelBedwars.getBedwarsGame();
        Language language = pixelBedwars.getLanguage();
        GamePlayer gamePlayer = bedwarsGame.getBedwarsPlayer(player);
        if (gamePlayer == null) {
            return;
        }
        bedwarsGame.sendMessage(gamePlayer, language.getJoinTeamMessage(), ImmutableMap.of("%team_name%", teamMeta.getFormatTeamName()));
        Objects.requireNonNull(team).addEntry(player.getName());
        String name = teamMeta.getTeamColor() + "[" + teamMeta.getTeamName() + "]" + player.getName();
        player.setDisplayName(name);
        player.setPlayerListName(name);
        applyPlayerTeamEquipment(player);
    }

    public void applyPlayerTeamEquipment(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        disableItemDrop(helmet);
        applyColorToLeather(helmet);
        inventory.setHelmet(helmet);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        disableItemDrop(chestplate);
        applyColorToLeather(chestplate);
        inventory.setChestplate(chestplate);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        disableItemDrop(leggings);
        applyColorToLeather(leggings);
        inventory.setLeggings(leggings);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        disableItemDrop(boots);
        applyColorToLeather(boots);
        inventory.setBoots(boots);
    }

    public void applyColorToLeather(ItemStack itemStack) {
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        itemMeta.setColor(colorFromName(ChatColor.getByChar(getTeamMeta().getTeamColor().charAt(1)).name()));
        itemStack.setItemMeta(itemMeta);
    }

    public Color colorFromName(String color) {
        try {
            return ((Color) Color.class.getField(color).get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void removePlayer(Player player) {
        Validate.notNull(player);
        Objects.requireNonNull(team).removeEntry(player.getName());
    }

    public boolean hasPlayer(Player player) {
        Validate.notNull(player);
        return Objects.requireNonNull(team).hasEntry(player.getName());
    }

    public List<GamePlayer> getTeamPlayers() {
        return getBedwarsGame().getGamePlayers()
                .stream()
                .filter(gamePlayer -> {
                    GameTeam playerTeam = bedwarsGame.getPlayerTeam(gamePlayer);
                    return playerTeam.equals(this);
                })
                .collect(Collectors.toList());
    }

    public List<GamePlayer> getAlivePlayers() {
        return getTeamPlayers()
                .stream()
                .filter(gamePlayer -> !gamePlayer.isTotallyDeath())
                .collect(Collectors.toList());
    }

    public boolean isBedDestroyed() {
        return getTeamMeta().getTeamBedLocation().toBukkitLocation().getBlock().getType() != Material.BED_BLOCK;
    }


}
