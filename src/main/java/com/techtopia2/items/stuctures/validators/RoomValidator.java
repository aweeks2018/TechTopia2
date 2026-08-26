package com.techtopia2.items.stuctures.validators;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import com.techtopia2.data.village.VillageData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.server.level.ServerLevel;

public abstract class RoomValidator
{
    private static final int DEFAULT_MAXIMUM_FLOOR_SPACES = 500;
    public static final int DEFAULT_MIN_FLOOR = 4;
    public static final int DEFAULT_MIN_ROOF = 2;
    public static final int DEFAULT_MAX_ROOF = 30;
    public static final int DEFAULT_MAX_SEARCH = 30;

    private final int minimumFloorSpaces;
    private final int maximumFloorSpaces;
    private final int minimumHeight;
    private final int maximumHeight;
    private final int maximumSearchSize;

    protected RoomValidator()
    {
        this(DEFAULT_MIN_FLOOR, DEFAULT_MIN_ROOF, DEFAULT_MAX_ROOF, DEFAULT_MAX_SEARCH); 
    }

    protected RoomValidator(int minimumFloorSpaces, int minimumHeight, int maximumHeight, int maximumSearchSize)
    {
        this(minimumFloorSpaces, DEFAULT_MAXIMUM_FLOOR_SPACES,
            minimumHeight, maximumHeight, maximumSearchSize);
        }

        protected RoomValidator(
            int minimumFloorSpaces,
            int maximumFloorSpaces,
            int minimumHeight,
            int maximumHeight,
            int maximumSearchSize)
        {
        this.minimumFloorSpaces = minimumFloorSpaces;
        this.maximumFloorSpaces = maximumFloorSpaces;
        this.minimumHeight = minimumHeight;
        this.maximumHeight = maximumHeight;
        this.maximumSearchSize = maximumSearchSize;
    }

    // Subclasses will override this method instead
    protected boolean checkCustomValidation(Level level, ItemFrame frame) 
    {
        return true; // Default behavior for base rooms
    }

    public final boolean isValid(Level level, ItemFrame frame)
    {
        if (requiresExistingVillage()
                && level instanceof ServerLevel serverLevel
                && !VillageData.isWithinVillage(serverLevel, frame.blockPosition()))
        {
            return false;
        }

        Direction outward = frame.getDirection();
        BlockPos framePosition = frame.blockPosition();
        BlockPos supportBlock = framePosition.relative(outward.getOpposite());
        BlockPos interiorStart = supportBlock.relative(outward.getOpposite());
        BlockPos floorStart = findFloorStart(level, interiorStart);
        if (floorStart == null)
        {
            return false;
        }
        Set<BlockPos> floorSpaces = findFloorSpaces(level, floorStart);

        if (floorSpaces.size() < minimumFloorSpaces || !hasSingleFloorLevel(floorSpaces))
        {
            return false;
        }

        int roofHeight = findRoofHeight(level, floorSpaces);
        return roofHeight >= minimumHeight
                && roofHeight <= maximumHeight
                && hasDoor(level, floorSpaces, roofHeight)
                && hasRoof(level, floorSpaces, roofHeight)
                && checkCustomValidation(level, frame);
    }

    protected boolean requiresExistingVillage()
    {
        return true;
    }

    protected BlockPos findFloorStart(Level level, BlockPos interiorStart)
    {
        for (int offset = 0; offset <= maximumHeight; offset++)
        {
            BlockPos candidate = interiorStart.below(offset);
            if (isFloorSpace(level, candidate))
            {
                return candidate;
            }
        }
        return null;
    }

    protected Set<BlockPos> findFloorSpaces(Level level, BlockPos start)
    {
        Set<BlockPos> floorSpaces = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();

        if (!isFloorSpace(level, start))
        {
            return floorSpaces;
        }

        pending.add(start);
        floorSpaces.add(start);
        int minimumX = start.getX() - maximumSearchSize / 2;
        int maximumX = minimumX + maximumSearchSize - 1;
        int minimumZ = start.getZ() - maximumSearchSize / 2;
        int maximumZ = minimumZ + maximumSearchSize - 1;

        while (!pending.isEmpty())
        {
            BlockPos current = pending.remove();
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                BlockPos next = current.relative(direction);
                if (next.getX() < minimumX || next.getX() > maximumX
                        || next.getZ() < minimumZ || next.getZ() > maximumZ)
                {
                    continue;
                }
                if (isFloorSpace(level, next) && floorSpaces.add(next))
                {
                    if (floorSpaces.size() > maximumFloorSpaces)
                    {
                        return new HashSet<>();
                    }
                    pending.add(next);
                }
            }
        }
        return floorSpaces;
    }

    private boolean isFloorSpace(Level level, BlockPos position)
    {
        BlockPos floorBlock = position.below();
        return !(level.getBlockState(position).getBlock() instanceof DoorBlock)
            && isOpenSpace(level, position)
            && !isOpenSpace(level, floorBlock);
    }

    private boolean isOpenSpace(Level level, BlockPos position)
    {
        return level.getBlockState(position).isAir()
                || !level.getBlockState(position).isCollisionShapeFullBlock(level, position);
    }

    private boolean hasSingleFloorLevel(Set<BlockPos> floorSpaces)
    {
        int floorY = floorSpaces.iterator().next().getY();
        return floorSpaces.stream().allMatch(position -> position.getY() == floorY);
    }

    private int findRoofHeight(Level level, Set<BlockPos> floorSpaces)
    {
        for (int roofHeight = maximumHeight; roofHeight >= minimumHeight; roofHeight--)
        {
            int candidateRoofHeight = roofHeight;
            boolean roofAtThisHeight = floorSpaces.stream().allMatch(floorSpace ->
                !isOpenSpace(level, floorSpace.above(candidateRoofHeight + 1)));
            if (roofAtThisHeight)
            {
                return roofHeight;
            }
        }
        return maximumHeight + 1;
    }

    private boolean hasRoof(Level level, Set<BlockPos> floorSpaces, int roofHeight)
    {
        return floorSpaces.stream().allMatch(floorSpace ->
                !isOpenSpace(level, floorSpace.above(roofHeight + 1)));
    }

    private boolean hasDoor(Level level, Set<BlockPos> floorSpaces, int roofHeight)
    {
        for (BlockPos floorSpace : floorSpaces)
        {
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                BlockPos boundary = floorSpace.relative(direction);
                if (level.getBlockState(boundary).getBlock() instanceof DoorBlock
                        && level.getBlockState(boundary.above()).getBlock() instanceof DoorBlock
                    && boundary.getY() == floorSpaces.iterator().next().getY())
                {
                    return true;
                }
            }
        }
        return false;
    }
}