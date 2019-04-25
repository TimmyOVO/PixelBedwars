package com.github.timmyovo.pixelbedwars.entity;

import com.github.timmyovo.pixelbedwars.entity.ai.AIDefendBedwarsBase;
import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.github.timmyovo.pixelbedwars.utils.NMSUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class BedwarsGolem extends EntityIronGolem {
    private GameTeam gameTeam;

    public BedwarsGolem(World world, GameTeam gameTeam) {
        super(world);
        this.gameTeam = gameTeam;
        NMSUtils.clearEntityAI(getBukkitEntity());
        initEntityAi();
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        setCustomName(getGolemHealth());
        setCustomNameVisible(true);
    }

    private String getGolemHealth() {
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

    public void initEntityAi() {
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.0D, true));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 0.9D, 32.0F));
        this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(2, new AIDefendBedwarsBase(this, EntityPlayer.class, true, gameTeam));
        this.targetSelector.a(2, new AIDefendBedwarsBase(this, EntitySilverfish.class, true, gameTeam));
    }

    @Override
    public void t_() {
        setCustomName(getGolemHealth());
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

    @Override
    protected void dropDeathLoot(boolean flag, int i) {
        //关闭掉落
    }
}
