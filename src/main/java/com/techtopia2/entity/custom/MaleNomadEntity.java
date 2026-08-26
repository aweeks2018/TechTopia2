package com.techtopia2.entity.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class MaleNomadEntity extends NomadEntity 
{
    // 1. Standard constructor required by NeoForge
    public MaleNomadEntity(EntityType<? extends NomadEntity> entityType, Level level) 
    {
        super(entityType, level);
    }

    // 3. Define the base attributes (Health, Speed, etc.)
    public static AttributeSupplier.Builder createAttributes() 
    {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)      // 10 hearts of health
                .add(Attributes.MOVEMENT_SPEED, 0.25D)  // Normal human walking speed
                .add(Attributes.FOLLOW_RANGE, 32.0D);   // How far away it can see targets
    }
}
