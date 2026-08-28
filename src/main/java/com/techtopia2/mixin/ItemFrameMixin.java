package com.techtopia2.mixin;

import com.techtopia2.data.village.BuildingManager;
import com.techtopia2.data.village.StructureType;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameMixin
{    
    @Inject(
        method = "interact",
        at = @At("RETURN")
    )
    private void onItemFrameInteract(
        net.minecraft.world.entity.player.Player player,
        net.minecraft.world.InteractionHand hand,
        net.minecraft.world.phys.Vec3 location,
        CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir)
    {
        if (player.getMainHandItem().is(Items.STICK) || player.getMainHandItem().isEmpty())
        {
            return;
        }

        ItemFrame frame = (ItemFrame)(Object)this;

        ItemStack item = frame.getItem();

        if (!(frame.level() instanceof ServerLevel level))
        {
            return;
        }

        if (item.isEmpty())
        {
            return;
        }

        if (!StructureType.isStructureItem(item))
        {
            return;
        }

        BuildingManager buildingManager = new BuildingManager();
        buildingManager.onStructureItemPlaced(level, player, frame);
    }
}