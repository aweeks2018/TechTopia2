package com.techtopia2;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BarracksValidator extends RoomValidator
{
    public static final BarracksValidator INSTANCE = new BarracksValidator();

    public final String FAILED_CUSTOM_VALIDTAION_MSG = "Barracks not validated becauce of: ";

    private BarracksValidator()
    {
        super();
    }

    private static boolean isArmorStand(Level level, BlockPos blockPos)
    {
        AABB box = new AABB(blockPos);
        // Search the level for armor stands inside that bounding box
        List<ArmorStand> armorStands = level.getEntitiesOfClass(ArmorStand.class, box);
        return !armorStands.isEmpty();
    }

    @Override
    protected boolean checkCustomValidation(Level level, ItemFrame frame) 
    {
        Direction outward = frame.getDirection();
        BlockPos framePosition = frame.blockPosition();
        BlockPos supportBlock = framePosition.relative(outward.getOpposite());
        BlockPos interiorStart = supportBlock.relative(outward.getOpposite());
        BlockPos floorStart = findFloorStart(level, interiorStart);

        boolean hasBed = false;
        boolean hasArmorStand = false; 
        for(BlockPos blockPos : findFloorSpaces(level, floorStart))
        {
            BlockPos blockPosAbove = blockPos.offset(0, 1, 0);
            BlockState blockAbove = level.getBlockState(blockPosAbove);
            if(blockAbove.is(BlockTags.BEDS))
            {
                hasBed = true;
            }

            if(isArmorStand(level, blockPosAbove))
            {
                hasArmorStand = true;
            }

            if(hasBed && hasArmorStand)
            {
                break;
            }
        }

        return hasBed && hasArmorStand;
    }
}