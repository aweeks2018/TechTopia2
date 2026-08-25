package com.techtopia2.entity;

import com.techtopia2.TechTopia2;
import com.techtopia2.entity.custom.FemaleNomadEntity;
import com.techtopia2.entity.custom.MaleNomadEntity;
import com.techtopia2.entity.custom.NomadEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;


@EventBusSubscriber(modid = TechTopia2.MOD_ID, value = Dist.CLIENT)
public final class ModEntities
{
    private static final Identifier MALE_TEXTURE = Identifier.fromNamespaceAndPath(
            "minecraft", "textures/entity/villager/villager.png");
    private static final Identifier FEMALE_TEXTURE = Identifier.fromNamespaceAndPath(
            "minecraft", "textures/entity/villager/villager.png");

    private ModEntities()
    {

    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(TechTopia2.MALE_NOMAD.get(), context ->
            new NomadRenderer<MaleNomadEntity>(context, MALE_TEXTURE));
        event.registerEntityRenderer(TechTopia2.FEMALE_NOMAD.get(), context ->
            new NomadRenderer<FemaleNomadEntity>(context, FEMALE_TEXTURE));
    }

    private static final class NomadRenderer<T extends NomadEntity>
        extends MobRenderer<T, VillagerRenderState, VillagerModel>
    {
        private NomadRenderer(EntityRendererProvider.Context context, Identifier texture)
        {
            super(context, new VillagerModel(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
            //addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this));
            this.texture = texture;
        }

        private final Identifier texture;

        @Override
        public VillagerRenderState createRenderState()
        {
            return new VillagerRenderState();
        }

        @Override
        public Identifier getTextureLocation(VillagerRenderState state)
        {
            return texture;
        }

    }
}
