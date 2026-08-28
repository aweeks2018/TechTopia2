package com.techtopia2.entity.navigation.movement;

import net.minecraft.core.BlockPos;

public record MovementResult(
    boolean valid,
    BlockPos destination,
    MovementType movementType,
    float cost,
    FailureReason failureReason
) {
    public static MovementResult invalid()
    {
        return new MovementResult(
            false,
            null,
            null,
            Float.POSITIVE_INFINITY,
            FailureReason.NOT_NEIGHBOR
        );
    }

    public static MovementResult blocked()
    {
        return new MovementResult(
            false,
            null,
            null,
            Float.POSITIVE_INFINITY,
            FailureReason.BLOCKED
        );
    }

    public static MovementResult success(
        BlockPos destination,
        MovementType movementType,
        float cost)
    {
        return new MovementResult(
            true,
            destination,
            movementType,
            cost,
            null
        );
    }
}