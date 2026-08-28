package com.techtopia2.entity.navigation.movement;

public record MovementProfile(
    int maxStepHeight,
    int maxDropHeight,
    int maxJumpDistance,
    boolean avoidsWater,
    boolean canOpenDoors
)
{
}
