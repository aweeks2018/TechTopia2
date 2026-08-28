package com.techtopia2.entity;

import com.techtopia2.TechTopia2;
import com.techtopia2.entity.custom.NomadEntity;
import com.techtopia2.entity.custom.FemaleNomadEntity;
import com.techtopia2.entity.custom.MaleNomadEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = TechTopia2.MOD_ID)
public final class TechTopiaEntities
{
    public static final DeferredRegister.Entities ENTITIES =
        DeferredRegister.createEntities(TechTopia2.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MaleNomadEntity>> MALE_NOMAD =
        ENTITIES.registerEntityType(
            "male_nomad",
            MaleNomadEntity::new,
            MobCategory.CREATURE,
            builder -> builder.sized(0.6F, 1.8F)
        );

    public static final DeferredHolder<EntityType<?>, EntityType<FemaleNomadEntity>> FEMALE_NOMAD =
        ENTITIES.registerEntityType(
            "female_nomad",
            FemaleNomadEntity::new,
            MobCategory.CREATURE,
            builder -> builder.sized(0.6F, 1.8F)
        );

        
    private TechTopiaEntities()
    {
    }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event)
    {
        event.put(
            MALE_NOMAD.get(),
            NomadEntity.createAttributes().build()
        );

        event.put(
            FEMALE_NOMAD.get(),
            NomadEntity.createAttributes().build()
        );
    }
}