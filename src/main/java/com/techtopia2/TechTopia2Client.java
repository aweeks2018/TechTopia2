package com.techtopia2;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TechTopia2.MOD_ID, value = Dist.CLIENT)
public final class TechTopia2Client
{
    private static final Identifier MALE_TEXTURE = Identifier.fromNamespaceAndPath(
            "minecraft", "textures/entity/player/wide/steve.png");
    private static final Identifier FEMALE_TEXTURE = Identifier.fromNamespaceAndPath(
            "minecraft", "textures/entity/player/slim/alex.png");

    private TechTopia2Client()
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
            extends MobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>>
    {
        private NomadRenderer(EntityRendererProvider.Context context, Identifier texture)
        {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
            addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this));
            this.texture = texture;
        }

        private final Identifier texture;

        @Override
        public HumanoidRenderState createRenderState()
        {
            return new HumanoidRenderState();
        }

        @Override
        public Identifier getTextureLocation(HumanoidRenderState state)
        {
            return texture;
        }
    }
}