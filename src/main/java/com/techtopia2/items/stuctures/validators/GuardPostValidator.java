package com.techtopia2.items.stuctures.validators;

import com.techtopia2.village.VillageData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;

public final class GuardPostValidator
{
    public static final GuardPostValidator INSTANCE = new GuardPostValidator();

    private GuardPostValidator()
    {
    }

    public boolean isValid(Level level, ItemFrame frame)
    {
        return level instanceof ServerLevel serverLevel
                && VillageData.isWithinVillage(serverLevel, frame.blockPosition());
    }
}