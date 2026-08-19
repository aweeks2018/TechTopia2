package com.techtopia2;

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
        return true;
    }
}