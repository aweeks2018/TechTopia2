package com.techtopia2;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;

public abstract class RoomValidator
{
    private final int minimumWidth;
    private final int minimumHeight;
    private final int minimumDepth;
    private final int minimumVolume;
    private final int maximumSearchSize;

    protected RoomValidator(
            int minimumWidth,
            int minimumHeight,
            int minimumDepth,
                int maximumSearchSize)
            {
        this(minimumWidth, minimumHeight, minimumDepth,
                minimumWidth * minimumHeight * minimumDepth, maximumSearchSize);
    }

    protected RoomValidator(
            int minimumWidth,
            int minimumHeight,
            int minimumDepth,
            int minimumVolume,
                int maximumSearchSize)
            {
        this.minimumWidth = minimumWidth;
        this.minimumHeight = minimumHeight;
        this.minimumDepth = minimumDepth;
        this.minimumVolume = minimumVolume;
        this.maximumSearchSize = maximumSearchSize;
    }

    public final boolean isValid(Level level, ItemFrame frame)
    {
        Direction inward = frame.getDirection();
        BlockPos supportBlock = frame.blockPosition().relative(inward.getOpposite());
        BlockPos roomOrigin = supportBlock.relative(inward);
        return hasEnclosedVolume(level, roomOrigin);
    }

    private boolean isOpenSpace(Level level, BlockPos position)
    {
        return level.getBlockState(position).isAir()
                || !level.getBlockState(position).isCollisionShapeFullBlock(level, position);
    }

    private boolean hasEnclosedVolume(Level level, BlockPos start)
    {
        int minX = start.getX() - (maximumSearchSize / 2);
        int minY = start.getY() - (maximumSearchSize / 2);
        int minZ = start.getZ() - (maximumSearchSize / 2);
        int maxX = minX + maximumSearchSize - 1;
        int maxY = minY + maximumSearchSize - 1;
        int maxZ = minZ + maximumSearchSize - 1;

        if (!isOpenSpace(level, start)) {
            return false;
        }

        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(start);
        visited.add(start);

        int minOpenX = start.getX();
        int minOpenY = start.getY();
        int minOpenZ = start.getZ();
        int maxOpenX = start.getX();
        int maxOpenY = start.getY();
        int maxOpenZ = start.getZ();

        while (!pending.isEmpty())
        {
            BlockPos current = pending.remove();
            minOpenX = Math.min(minOpenX, current.getX());
            minOpenY = Math.min(minOpenY, current.getY());
            minOpenZ = Math.min(minOpenZ, current.getZ());
            maxOpenX = Math.max(maxOpenX, current.getX());
            maxOpenY = Math.max(maxOpenY, current.getY());
            maxOpenZ = Math.max(maxOpenZ, current.getZ());

            for (Direction direction : Direction.values())
            {
                BlockPos next = current.relative(direction);
                if (!isInsideSearchBox(next, minX, minY, minZ, maxX, maxY, maxZ))
                {
                    if (isOpenSpace(level, next))
                    {
                        return false;
                    }
                    continue;
                }
                if (isOpenSpace(level, next) && visited.add(next))
                {
                    pending.add(next);
                }
            }
        }

        int openWidth = maxOpenX - minOpenX + 1;
        int openHeight = maxOpenY - minOpenY + 1;
        int openDepth = maxOpenZ - minOpenZ + 1;
        return visited.size() >= minimumVolume
                && openWidth >= minimumWidth
                && openHeight >= minimumHeight
                && openDepth >= minimumDepth;
    }

    private boolean isInsideSearchBox(
            BlockPos position,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
                int maxZ)
            {
        return position.getX() >= minX && position.getX() <= maxX
                && position.getY() >= minY && position.getY() <= maxY
                && position.getZ() >= minZ && position.getZ() <= maxZ;
    }
}