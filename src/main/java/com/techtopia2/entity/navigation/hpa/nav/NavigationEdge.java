package com.techtopia2.entity.navigation.hpa.nav;

public class NavigationEdge
{
    private final NavigationNode destination;
    private final double cost;

    public NavigationEdge(NavigationNode destination, double cost)
    {
        this.destination = destination;
        this.cost = cost;
    }
}