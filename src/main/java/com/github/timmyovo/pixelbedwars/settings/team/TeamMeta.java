package com.github.timmyovo.pixelbedwars.settings.team;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.timmyovo.pixelbedwars.settings.resource.ResourceSpawner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamMeta {
    private String teamName;
    private String teamColor;
    private int minPlayer;
    private int maxPlayer;
    private VecLoc3D teamGameLocation;
    private String woolColor;
    private VecLoc3D teamBedLocation;
    private List<ResourceSpawner> ironSpawnerList;
    private List<ResourceSpawner> goldSpawnerList;
    private List<ResourceSpawner> diamondSpawnerList;
    private List<ResourceSpawner> emeraldSpawnerList;

    public String getFormatTeamName() {
        return getTeamColor() + getTeamName();
    }

    public ItemStack getWool() {
        ItemStack itemStack = new ItemStack(Material.WOOL);
        itemStack.setDurability(DyeColor.valueOf(woolColor).getData());
        return itemStack;
    }

    public void tickSpawner() {
        getIronSpawnerList().forEach(ResourceSpawner::tickInterval);
        getGoldSpawnerList().forEach(ResourceSpawner::tickInterval);
        getDiamondSpawnerList().forEach(ResourceSpawner::tickInterval);
        getEmeraldSpawnerList().forEach(ResourceSpawner::tickInterval);
    }

    public void allSpawnerMultiplier(int multiplier) {
        for (ResourceSpawner.SpawnerType value : ResourceSpawner.SpawnerType.values()) {
            setResourceSpawnerMultiplier(value, multiplier);
        }
    }

    public void setResourceSpawnerMultiplier(ResourceSpawner.SpawnerType spawnerType, int multiplier) {
        switch (spawnerType) {
            case IRON:
                this.ironSpawnerList.forEach(resourceSpawner -> resourceSpawner.setMultiplier(multiplier));
                break;
            case GOLD:
                this.goldSpawnerList.forEach(resourceSpawner -> resourceSpawner.setMultiplier(multiplier));
                break;
            case DIAMOND:
                this.diamondSpawnerList.forEach(resourceSpawner -> resourceSpawner.setMultiplier(multiplier));
                break;
            case EMERALD:
                this.emeraldSpawnerList.forEach(resourceSpawner -> resourceSpawner.setMultiplier(multiplier));
                break;
        }
    }
}
