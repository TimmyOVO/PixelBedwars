package com.github.timmyovo.pixelbedwars.settings.team;

import com.github.skystardust.ultracore.bukkit.models.VecLoc3D;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
