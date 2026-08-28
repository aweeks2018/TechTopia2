package com.techtopia2.entity.navigation.hpa.portal;

import java.util.ArrayList;
import java.util.List;

import com.techtopia2.entity.navigation.hpa.cluster.Cluster;

import net.minecraft.core.BlockPos;

public class Portal
{
    private final Cluster first;
    private final Cluster second;

    private final List<BlockPos> positions;

    public Portal(Cluster firstCluster, Cluster secondCluster)
    {
        first = firstCluster;
        second = secondCluster;
        this.positions = new ArrayList<>();
    }

    public Portal(Cluster firstCluster, Cluster secondCluster, ArrayList<BlockPos> positions)
    {
        first = firstCluster;
        second = secondCluster;
        this.positions = positions;
    }
}