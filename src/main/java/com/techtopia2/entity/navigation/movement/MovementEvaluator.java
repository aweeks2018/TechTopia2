package com.techtopia2.entity.navigation.movement;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class MovementEvaluator
{
    public static final Logger LOGGER = LogUtils.getLogger();

    private final MovementProfile profile;
    private final WalkNodeEvaluator nodeEvaluator;

    public MovementEvaluator(MovementProfile profile)
    {
        this.profile = profile;
        this.nodeEvaluator = new WalkNodeEvaluator();
    }

    public MovementResult evaluate(
            Mob mob,
            PathNavigationRegion level,
            BlockPos from, 
            BlockPos to)
    {
        // 1. Verify that the positions are neighbors
        if (!isNeighbor(from, to))
        {
            return MovementResult.invalid();
        }

        // Prepare Minecraft's node evaluator for this mob/level
        nodeEvaluator.prepare(level, mob);
        
        try
        {
            // 2. Check for a closed door
            if (isClosedDoor(mob, to))
            {
                if (!canMobOpenDoor(mob, to))
                {
                    return MovementResult.blocked();
                }

                return MovementResult.success(
                        to,
                        MovementType.OPEN_DOOR,
                        calculateCost(MovementType.OPEN_DOOR)
                );
            }

            // 3. Determine movement type
            MovementType movementType =
                    determineMovementType(mob, from, to);

            // 4. Check destination
            if (!isDestinationValid(mob, to))
            {
                return MovementResult.blocked();
            }

            // 5. Check vertical movement
            if (!canMoveVertically(mob, from, to))
            {
                return MovementResult.blocked();
            }

            // 6. Check physical clearance
            if (!hasClearance(mob, from, to))
            {
                return MovementResult.blocked();
            }

            // 7. Check special blocks
            if (isWater(mob, to))
            {
                return MovementResult.blocked();
            }

            if (isFence(mob, to))
            {
                return MovementResult.blocked();
            }

            // 8. Calculate movement cost
            float cost = calculateCost(movementType);

            // 9. Return successful movement
            return MovementResult.success(
                    to,
                    movementType,
                    cost
            );
        }
        finally
        {
            // Release evaluator resources
            nodeEvaluator.done();
        }
    }
    /**
     * Determines whether this mob is capable of opening the door.
     */
    private boolean canMobOpenDoor(Mob mob, BlockPos position)
    {
        /*
         * Example:
         *
         * if (mob instanceof NomadEntity)
         * {
         *     return true;
         * }
         */

        // TODO: Add mob-specific door-opening rules.
        return true;
    }

    /**
     * Determines whether the destination contains a closed door.
     */
    private boolean isClosedDoor(Mob mob, BlockPos position)
    {
        Level level = mob.level();
        BlockState state = level.getBlockState(position);

        return state.getBlock() instanceof DoorBlock
                && !state.getValue(DoorBlock.OPEN);
    }

    /**
     * Determines what action the mob needs to perform
     * to move from one position to another.
     */
    private MovementType determineMovementType(
            Mob mob,
            BlockPos from,
            BlockPos to)
    {
        int dy = to.getY() - from.getY();

        if (dy > 0)
        {
            return MovementType.JUMP;
        }

        if (dy < 0)
        {
            return MovementType.DROP;
        }

        return MovementType.WALK;
    }

    /**
     * Determines whether the destination is one movement
     * step away from the current position.
     */
    private boolean isNeighbor(BlockPos from, BlockPos to)
    {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());

        return dx <= 1
                && dy <= 1
                && dz <= 1
                && (dx + dy + dz > 0);
    }

    /**
     * Uses Minecraft's pathfinding node evaluator to determine
     * whether the destination is a valid path node.
     */
    private boolean isDestinationValid(Mob mob, BlockPos position)
    {
        PathType pathType = nodeEvaluator.getPathType(mob, position);

        return pathType != PathType.BLOCKED;
    }

    /**
     * Determines whether the mob's physical bounding box can
     * occupy the destination position.
     */
    private boolean hasClearance(
            Mob mob,
            BlockPos from,
            BlockPos to)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();

        var destinationBox = mob.getBoundingBox().move(dx, dy, dz);

        return mob.level().noCollision(mob, destinationBox);
    }

    /**
     * Determines whether the destination contains water.
     */
    private boolean isWater(Mob mob, BlockPos position)
    {
        Level level = mob.level();
        BlockState state = level.getBlockState(position);

        return state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
    }

    /**
     * Determines whether the destination contains a fence.
     */
    private boolean isFence(Mob mob, BlockPos position)
    {
        Level level = mob.level();
        BlockState state = level.getBlockState(position);

        return state.is(BlockTags.FENCES);
    }

    /**
     * Determines whether the vertical movement is physically
     * possible for this mob.
     */
    private boolean canMoveVertically(
            Mob mob,
            BlockPos from,
            BlockPos to)
    {
        int dy = to.getY() - from.getY();

        // Normal horizontal movement.
        if (dy == 0)
        {
            return true;
        }

        // Only allow one-block vertical changes.
        if (Math.abs(dy) > 1)
        {
            return false;
        }

        // Moving upward requires a jump.
        if (dy > 0)
        {
            return true;
        }

        // Moving downward is allowed as a drop.
        return true;
    }

    /**
     * Calculates the base movement cost.
     */
    private float calculateCost(MovementType movementType)
    {
        return switch (movementType)
        {
            case WALK -> 1.0F;
            case JUMP -> 1.5F;
            case DROP -> 1.0F;
            case OPEN_DOOR -> 2.0F;
        };
    }
}
