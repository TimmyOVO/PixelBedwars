package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import com.github.timmyovo.pixelbedwars.settings.team.TeamMeta;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class BedwarsEgg extends EntityEgg {
    private int locXPrev;
    private int locYPrev;
    private int locZPrev;
    private int currentBlock;

    private TeamMeta teamMeta;

    public BedwarsEgg(World world, EntityLiving entityliving, TeamMeta teamMeta) {
        super(world, entityliving);
        this.teamMeta = teamMeta;
    }

    public static void shoot(org.bukkit.World world, Player player, TeamMeta teamMeta) {
        new BedwarsEgg(((CraftWorld) world).getHandle(), ((CraftPlayer) player).getHandle(), teamMeta).spawnEntity();
    }

    @Override
    public void t_() {
        super.t_();
        if (currentBlock >= 60) {
            die();
        }
        Block blockAt = this.world.getWorld().getBlockAt(locXPrev, locYPrev, locZPrev);
        blockAt.setTypeIdAndData(org.bukkit.Material.WOOL.getId(), ((byte) teamMeta.getWool().getDurability()), true);
        blockAt.setMetadata("human", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), true));
        Block blockAt1 = this.world.getWorld().getBlockAt(locXPrev + 1, locYPrev, locZPrev);
        blockAt1.setTypeIdAndData(org.bukkit.Material.WOOL.getId(), ((byte) teamMeta.getWool().getDurability()), true);
        blockAt1.setMetadata("human", new FixedMetadataValue(PixelBedwars.getPixelBedwars(), true));
        currentBlock++;
        locXPrev = (int) lastX;
        locYPrev = (int) lastY;
        locZPrev = (int) lastZ;
    }

    public void spawnEntity() {
        this.world.addEntity(this);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        boolean hatching = !this.world.isClientSide && this.random.nextInt(8) == 0;
        int numHatching = this.random.nextInt(32) == 0 ? 4 : 1;
        if (!hatching) {
            numHatching = 0;
        }

        EntityType hatchingType = EntityType.CHICKEN;
        Entity shooter = this.getShooter();
        if (shooter instanceof EntityPlayer) {
            Player player = shooter == null ? null : (Player) shooter.getBukkitEntity();
            PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, (Egg) this.getBukkitEntity(), hatching, (byte) numHatching, hatchingType);
            this.world.getServer().getPluginManager().callEvent(event);
            hatching = event.isHatching();
            numHatching = event.getNumHatches();
            hatchingType = event.getHatchingType();
        }

        int j;
        if (hatching) {
            for (j = 0; j < numHatching; ++j) {
                Entity entity = this.world.getWorld().createEntity(new Location(this.world.getWorld(), this.locX, this.locY, this.locZ, this.yaw, 0.0F), hatchingType.getEntityClass());
                if (entity.getBukkitEntity() instanceof Ageable) {
                    ((Ageable) entity.getBukkitEntity()).setBaby();
                }

                // this.world.getWorld().addEntity(entity, CreatureSpawnEvent.SpawnReason.EGG);
            }
        }

        for (j = 0; j < 8; ++j) {
            this.world.addParticle(EnumParticle.ITEM_CRACK, this.locX, this.locY, this.locZ, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, new int[]{Item.getId(Items.EGG)});
        }

        if (!this.world.isClientSide) {
            this.die();
        }

    }
}
