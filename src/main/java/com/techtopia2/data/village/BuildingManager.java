package com.techtopia2.data.village;

import com.techtopia2.data.village.VillageData;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.techtopia2.data.village.StructureType;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BuildingManager
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public void onStructureItemPlaced(
        ServerLevel level,
        Player player,
        ItemFrame frame)
    {
        ItemStack item = frame.getItem();

        if (item.isEmpty())
        {
            LOGGER.info("Frame was empty");
            return;
        }

        if (!StructureType.isStructureItem(item))
        {
            LOGGER.info("Item in frame was not a StructureType");
            return;
        }

        StructureType type =
            StructureType.getStructureTypeFromItem(item);

        if (!type.validator().isValid(level, frame))
        {
            LOGGER.info("Structure was not valid");
            return;
        }

        if(VillageData.get(level).registerBuilding(frame.blockPosition(), type, player, level))
        {
            player.sendSystemMessage(Component.literal(type.displayName() + " structure registered to the village."));
        }
        else
        {
            LOGGER.info("Structure {} was NOT resgistered", type.displayName());
        }
    }

}
