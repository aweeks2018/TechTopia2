package com.techtopia2.event;

import java.util.Optional;

import com.techtopia2.data.village.BuildingManager;
import com.techtopia2.data.village.StructureType;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class BuildingEvents
{
    private BuildingEvents()
    {
    }

    @SubscribeEvent
    public static void onItemFrameInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (!(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }

        if (!(event.getTarget() instanceof ItemFrame frame))
        {
            return;
        }

        Player player = event.getEntity();

        if (!player.getMainHandItem().is(Items.STICK) || player.getMainHandItem().isEmpty())
        {
            return;
        }

        BuildingManager buildingManager = new BuildingManager();
        Optional<StructureType> type = buildingManager.unregisterBuilding(frame.blockPosition(), level );

        if (type.isEmpty())
        {
            return;
        }

        String displayName = type.get().displayName();

        player.sendSystemMessage(
            Component.literal(
                displayName.substring(0, 1).toUpperCase()
                + displayName.substring(1)
                + " at "
                + frame.blockPosition().getX() + ", "
                + frame.blockPosition().getY() + ", "
                + frame.blockPosition().getZ()
                + " has been unregistered from the village."
            )
        );
    }
}