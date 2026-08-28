package com.techtopia2.entity.navigation.hpa.nav;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;

public class NavigationNode
{
    private final int id;
    private final BlockPos position;

    private final List<NavigationEdge> edges;

    public NavigationNode(int id, BlockPos position)
    {
        this.id = id;
        this.position = position;
        edges = new ArrayList<>();
    }
}
