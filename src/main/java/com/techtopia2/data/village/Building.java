package com.techtopia2.data.village;

import net.minecraft.core.BlockPos;

public final class Building 
{
    private final BlockPos position;
    private final String type; 

    public Building(BlockPos position, String type) 
    {
        this.position = position;
        this.type = type;
    }

    public BlockPos position() 
    {
        return position;
    }

    public String type() 
    {
        return type;
    }
}
