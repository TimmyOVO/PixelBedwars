package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.entity.ai.AIDefendBedwarsBase;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class BedwarsSilverfish extends EntitySilverfish {
    private GameTeam gameTeam;

    public BedwarsSilverfish(World world, GameTeam gameTeam) {
        super(world);
        this.gameTeam = gameTeam;
        NMSUtils.clearEntityAI(getBukkitEntity());
        initEntityAi();
    }

    private void initEntityAi() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(2, new AIDefendBedwarsBase(this, EntityPlayer.class, true, gameTeam));
        this.targetSelector.a(2, new AIDefendBedwarsBase(this, EntityIronGolem.class, true, gameTeam));
    }

    @Override
    public void t_() {
        setCustomName(getSilverfishHealth());
        super.t_();
    }

    public void spawnEntity(Location location) {
        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.world.addEntity(this);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer) {
            EntityPlayer damagesourceEntity = (EntityPlayer) damagesource.getEntity();
            if (gameTeam.hasPlayer(damagesourceEntity.getBukkitEntity())) {
                return false;
            }
        }
        return super.damageEntity(damagesource, f);
    }


    private String getSilverfishHealth() {
        StringBuilder stringBuilder = new StringBuilder("§7[");
        float v = getHealth() / getMaxHealth();
        double progress = Math.floor(v * 10);
        for (int i = 0; i < 10; i++) {
            if (i <= progress) {
                stringBuilder.append(ChatColor.GREEN);
            } else {
                stringBuilder.append(ChatColor.GRAY);
            }
            stringBuilder.append("■");
        }
        stringBuilder.append("§7]");
        return stringBuilder.toString();
    }

    @Override
    protected void dropDeathLoot(boolean flag, int i) {

    }
}
