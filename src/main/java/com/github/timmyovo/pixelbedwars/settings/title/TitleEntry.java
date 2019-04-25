package com.github.timmyovo.pixelbedwars.settings.title;

import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TitleEntry {
    private String title;
    private String subtitle;

    public void sendToPlayer(Player player, ImmutableMap<String, String> var) {
        NMSUtils.sendPlayerTitle(player, getTitle(), var);
        NMSUtils.sendPlayerSubtitle(player, getSubtitle(), var);
    }
}
