package com.techtopia2.entity.custom;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.techtopia2.entity.goals.SeekBuidling;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class NomadEntity extends PathfinderMob
{
    private static final Logger LOGGER = LogUtils.getLogger();

    protected NomadEntity(EntityType<? extends NomadEntity> entityType, Level level)
    {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() 
    {
        // High Priority: Don't drown
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        // Mid Priority: Run if hurt, otherwise stroll around
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        //this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        
        // Low Priority: Social behaviors
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(4, new SeekBuidling(this));

        LOGGER.info("registerGoals has finihed");
    }

}