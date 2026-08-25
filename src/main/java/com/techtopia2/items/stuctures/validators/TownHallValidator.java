package com.techtopia2.items.stuctures.validators;

public final class TownHallValidator extends RoomValidator
{
    public static final TownHallValidator INSTANCE = new TownHallValidator();

    private TownHallValidator()
    {
        super();
    }

    @Override
    protected boolean requiresExistingVillage()
    {
        return false;
    }
}