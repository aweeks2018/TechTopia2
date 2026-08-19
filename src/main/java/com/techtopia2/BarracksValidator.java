package com.techtopia2;

public final class BarracksValidator extends RoomValidator
{
    public static final BarracksValidator INSTANCE = new BarracksValidator();

    private BarracksValidator()
    {
        super(3, 3, 3, 30);
    }
}