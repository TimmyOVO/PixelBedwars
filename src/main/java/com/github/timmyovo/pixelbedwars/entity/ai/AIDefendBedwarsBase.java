package com.github.timmyovo.pixelbedwars.entity.ai;

import com.github.timmyovo.pixelbedwars.game.GameTeam;
import com.google.common.base.Predicate;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PathfinderGoalNearestAttackableTarget;

public class AIDefendBedwarsBase extends PathfinderGoalNearestAttackableTarget {
    private GameTeam gameTeam;

    public AIDefendBedwarsBase(EntityCreature entitycreature, Class oclass, boolean flag, GameTeam gameTeam) {
        super(entitycreature, oclass, flag);
        this.gameTeam = gameTeam;
    }

    public AIDefendBedwarsBase(EntityCreature entitycreature, Class oclass, boolean flag, boolean flag1) {
        super(entitycreature, oclass, flag, flag1);
    }

    public AIDefendBedwarsBase(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1, Predicate predicate) {
        super(entitycreature, oclass, i, flag, flag1, predicate);
    }

    @Override
    public boolean a() {
        boolean a = super.a();
        if (this.d != null) {
            if (this.d instanceof EntityPlayer) {
                if (gameTeam.hasPlayer(((EntityPlayer) this.d).getBukkitEntity())) {
                    this.d = null;
                    return false;
                }
            }
        }
        return a;
    }
}
