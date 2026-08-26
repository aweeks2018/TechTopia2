package com.techtopia2.village;

import java.util.ArrayList;
import java.util.List;

public final class Village 
{
    private final Building townHall;
    private final List<Building> buildings;

    // This constructor ensures that whenever Minecraft creates a Village,
    // the list of buildings is forced into an unlocked ArrayList!
    public Village(Building townHall, List<Building> buildings) 
    {
        this.townHall = townHall;
        this.buildings = new ArrayList<>(buildings);
    }

    public Building townHall() 
    {
        return this.townHall;
    }

    public List<Building> buildings() 
    {
        return this.buildings;
    }

}