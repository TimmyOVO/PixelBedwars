package com.github.timmyovo.pixelbedwars.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface Corpses {

    CorpseData spawnCorpse(Player p, String overrideName, Location loc, Inventory items, int facing);

    CorpseData loadCorpse(String gpName, String gpJSON, Location loc, Inventory items, int facing);

    void removeCorpse(CorpseData data);

    int getNextEntityId();

    List<CorpseData> getAllCorpses();

    void registerPacketListener(Player p);

    interface CorpseData {

        void resendCorpseToEveryone();

        void resendCorpseToPlayer(Player p);

        void destroyCorpseFromEveryone();

        void destroyCorpseFromPlayer(Player p);

        boolean mapContainsPlayer(Player p);

        void setCanSee(Player p, boolean b);

        boolean canSee(Player p);

        Set<Player> getPlayersWhoSee();

        void removeFromMap(Player p);

        void removeAllFromMap(Collection<Player> toRemove);

        Location getTrueLocation();

        Location getOrigLocation();

        int getTicksLeft();

        void setTicksLeft(int ticksLeft);

        void tickPlayerLater(int ticks, Player p);

        int getPlayerTicksLeft(Player p);

        boolean isTickingPlayer(Player p);

        void stopTickingPlayer(Player p);

        Set<Player> getPlayersTicked();

        int getEntityId();

        Inventory getLootInventory();

        InventoryView getInventoryView();

        void setInventoryView(InventoryView iv);

        String getKillerUsername();

        UUID getKillerUUID();

        int getSelectedSlot();

        CorpseData setSelectedSlot(int slot);

        int getRotation();

        String getCorpseName();

        String getProfilePropertiesJson();

    }
}
