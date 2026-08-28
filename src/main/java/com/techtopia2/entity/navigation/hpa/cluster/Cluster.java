package com.techtopia2.entity.navigation.hpa.cluster;

import java.util.List;

import com.techtopia2.entity.navigation.hpa.portal.Portal;
public class Cluster
{
    private final ClusterPos position;
    private final BoundingBox bounds;

    private final List<Portal> portals;

    private boolean dirty;
}
