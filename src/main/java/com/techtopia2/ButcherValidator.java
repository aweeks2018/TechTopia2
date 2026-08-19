package com.techtopia2;

public final class ButcherValidator extends RoomValidator
{
    public static final ButcherValidator INSTANCE = new ButcherValidator();

    private ButcherValidator()
    {
        super(1, 1, 1, 8, 30);
    }
}