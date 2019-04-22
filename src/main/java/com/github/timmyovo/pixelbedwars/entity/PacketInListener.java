package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Field;

public class PacketInListener extends ChannelInboundHandlerAdapter {

    private Player p;

    public PacketInListener(Player p) {
        this.p = p;
    }

    public static final void registerListener(Player p) {
        Channel c = getChannel(p);
        if (c == null) {
            throw new NullPointerException("Couldn't get channel??");
        }
        c.pipeline().addBefore("packet_handler", "packet_in_listener",
                new PacketInListener(p));
    }

    public static final Channel getChannel(Player p) {
        NetworkManager nm = ((CraftPlayer) p).getHandle().playerConnection.networkManager;
        try {
            return nm.channel;
			/*
			Field ifield = nm.getClass().getDeclaredField("channel");
			ifield.setAccessible(true);
			Channel c = (Channel) ifield.get(nm);
			ifield.setAccessible(false);
			return c;
			*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof PacketPlayInUseEntity) {
            final PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;
            Bukkit.getServer().getScheduler()
                    .runTask(PixelBedwars.getPixelBedwars(), () -> {
                        if (packet.a() == EnumEntityUseAction.INTERACT_AT || packet.a() == EnumEntityUseAction.INTERACT) {

                            for (Corpses.CorpseData cd : PixelBedwars.getPixelBedwars().getCorpsesManager()
                                    .getAllCorpses()) {
                                if (cd.getEntityId() == getId(packet)) {
                                    InventoryView view = p.openInventory(cd
                                            .getLootInventory());
                                    cd.setInventoryView(view);
                                    break;
                                }
                            }
                        }
                    });
        }
        super.channelRead(ctx, msg);
    }

    private int getId(PacketPlayInUseEntity packet) {
        try {
            Field afield = packet.getClass().getDeclaredField("a");
            afield.setAccessible(true);
            int id = afield.getInt(packet);
            afield.setAccessible(false);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
