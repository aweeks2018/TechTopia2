package com.techtopia2;

public final class TownHallValidator extends RoomValidator
{
    public static final TownHallValidator INSTANCE = new TownHallValidator();

    private TownHallValidator()
    {
        super(4, 2, 30, 30);
    }
}