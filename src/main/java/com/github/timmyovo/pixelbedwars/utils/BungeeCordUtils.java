package com.github.timmyovo.pixelbedwars.utils;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

public class BungeeCordUtils {
    public static void sendPlayer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(PixelBedwars.getPixelBedwars(), "BungeeCord", out.toByteArray());
    }
}
