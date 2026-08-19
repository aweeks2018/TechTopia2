package com.techtopia2;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;

public final class GuardPostValidator extends RoomValidator
{
    public static final GuardPostValidator INSTANCE = new GuardPostValidator();

    private GuardPostValidator()
    {
        super(4, 2, 30, 30);
    }

}