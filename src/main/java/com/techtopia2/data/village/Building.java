package com.techtopia2.data.village;
import net.minecraft.core.BlockPos;

public final class Building 
{
    private final BlockPos position;
    private final StructureType type; 

    public Building(BlockPos position, StructureType type) 
    {
        this.position = position;
        this.type = type;
    }

    public BlockPos position() 
    {
        return position;
    }

    public StructureType type() 
    {
        return type;
    }
}
