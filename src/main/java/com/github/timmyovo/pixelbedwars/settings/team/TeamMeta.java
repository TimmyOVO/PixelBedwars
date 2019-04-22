package com.github.timmyovo.pixelbedwars.settings.team;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import com.github.timmyovo.pixelbedwars.settings.resource.ResourceSpawner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public void tickSpawner() {
        getIronSpawnerList().forEach(ResourceSpawner::tickInterval);
        getGoldSpawnerList().forEach(ResourceSpawner::tickInterval);
        getDiamondSpawnerList().forEach(ResourceSpawner::tickInterval);
        getEmeraldSpawnerList().forEach(ResourceSpawner::tickInterval);
    }
}
