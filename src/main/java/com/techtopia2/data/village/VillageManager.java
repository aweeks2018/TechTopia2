package com.techtopia2.data.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class VillageManager 
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final int VILLAGE_RADIUS = 250;

    public VillageManager()
    {
    }
    
    public boolean registerTownHall(BlockPos position, ServerLevel level)
    {
        VillageData data = VillageData.get(level);

        if (findVillage(position, level).isPresent())
        {
            LOGGER.info("registerTownHall: Already inside a village");
            return false;
        }

        BuildingManager buildingManager = new BuildingManager();

        buildingManager.updateBuildingMarker(level, position, true);
 
        Building newTownHall = new Building(position, StructureType.TOWN_HALL);
        Village newVillage = new Village(newTownHall, new ArrayList<>());

        Iterator<Building> iterator = data.unregisteredBuildings().iterator();

        while (iterator.hasNext())
        {
            Building building = iterator.next();

            if (isWithinVillage(building.position(), level, newVillage))
            {
                buildingManager.updateBuildingMarker(level, building.position(), true);

                LOGGER.info("Adding {} building to new village", building.type().displayName());

                newVillage.buildings().add(building);
                iterator.remove();
            }
        }
       
        data.addVillage(newVillage);

        return true;
    }

    public boolean isWithinAnyVillage(ServerLevel level, BlockPos position)
    {
        return findVillage(position, level).isPresent();
    }

    public static boolean isWithinVillage(BlockPos position, ServerLevel level, Village village)
    {
        // Force long casting to completely prevent any math integer overflows
        long radiusSquared = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;

        BlockPos hallPos = village.townHall().position();

         // 1. Calculate the flat 2D distance offsets as long values
        long dx = (long) hallPos.getX() - position.getX();
        long dz = (long) hallPos.getZ() - position.getZ();
        
        // 2. Compute the 2D squared distance
        long distance2D = (dx * dx) + (dz * dz);
        
        // 3. Return true if the entity is inside the 2D flat circle boundary
        return distance2D <= radiusSquared;
    }

    public Optional<Village> findVillage(BlockPos position, ServerLevel level)
    {
        return VillageData.get(level).villages().stream()
                .filter(village -> isWithinVillage(position, level, village))
                .findFirst();
    }

    public boolean forceDeleteAllVillages(Level level) 
    {
        VillageData data = VillageData.get((ServerLevel)level);

        BuildingManager buildingManager = new BuildingManager();
        // 1. Loop through and clear any block states / visual data if needed
        for (Village village : data.villages()) 
        {
            buildingManager.updateBuildingMarker(level, village.townHall().position(), false);
            for (Building building : village.buildings()) 
            {
                buildingManager.updateBuildingMarker(level, building.position(), false);
            }
        }
        
        // 2. Completely dump out the data pools
        data.villages().clear();
        data.unregisteredBuildings().clear();
        
        // 3. Force save the cleared empty lists to the hard drive immediately
        data.setDirty();
        LOGGER.info("!!! DANGER !!! All villages have been forcefully purged from the database.");
        return true;
    }


    public void validateVillages(ServerLevel level)
    {
        VillageData data = VillageData.get(level);
        BuildingManager buildingManager = new BuildingManager();

        LOGGER.info("validateVillages triggered ... ");

        List<Building> buildingsToUnregister = new ArrayList<>();

        for (Village village : data.villages())
        {
            for (Building building : village.buildings())
            {
                for (ItemFrame itemFrame : level.getEntitiesOfClass(
                        ItemFrame.class,
                        new AABB(building.position())))
                {
                    if (StructureType.isStructureItem(itemFrame.getItem()))
                    {
                        StructureType frameType =
                            StructureType.getStructureTypeFromItem(itemFrame.getItem());

                        // Marker doesn't match registered building
                        if (!frameType.equals(building.type()))
                        {
                            LOGGER.info(
                                "The building marker for the {} at {} does not match the " +
                                "type of the building registered. The current item frame " +
                                "has the building type {}.",
                                building.type().displayName(),
                                building.position(),
                                frameType.displayName()
                            );

                            buildingsToUnregister.add(building);
                            continue;
                        }

                        // Building structure is invalid
                        if (!building.type().validator().isValid(level, itemFrame))
                        {
                            LOGGER.info(
                                "{} at {} was not valid. Unregistering building now ...",
                                building.type().displayName(),
                                building.position()
                            );

                            buildingsToUnregister.add(building);
                        }
                    }
                    else
                    {
                        LOGGER.info(
                            "{} at {} {}. Unregistering building now ...",
                            building.type().displayName(),
                            building.position(),
                            itemFrame.getItem().isEmpty()
                                ? "has an empty item frame"
                                : "does not have a StructureType item in the frame"
                        );

                        buildingsToUnregister.add(building);
                    }
                }
            }
        }

        // Modify the village data AFTER iteration is complete
        for (Building building : buildingsToUnregister)
        {
            buildingManager.unregisterBuilding(building.position(), level);
        }

        List<Building> buildingsToRemoveFromUnregisteredBuildings = new ArrayList<>();

        for (Building building : data.unregisteredBuildings())
        {
            if(isWithinAnyVillage(level, building.position()))
            {
                for (ItemFrame itemFrame : level.getEntitiesOfClass(
                        ItemFrame.class,
                        new AABB(building.position())))
                {
                    if (building.type().validator().isValid(level, itemFrame))
                    {
                        buildingsToRemoveFromUnregisteredBuildings.add(building);
                    }
                }
            }
        }

        for (Building building : buildingsToRemoveFromUnregisteredBuildings)
        {
            buildingManager.registerBuilding(building, level);
        }
    }
}


/*
for (ItemFrame itemFrame : level.getEntitiesOfClass(
                        ItemFrame.class,
                        new AABB(village.townHall().position())))
            {
                Building building = village.townHall();
                if (StructureType.isStructureItem(itemFrame.getItem()))
                {
                    StructureType frameType = StructureType.getStructureTypeFromItem(itemFrame.getItem());

                        // Marker doesn't match registered building
                    if (!frameType.equals(building.type()))
                    {
                        LOGGER.info(
                            "The building marker for the {} at {} does not match the " +
                            "type of the building registered. The current item frame " +
                            "has the building type {}.",
                            building.type().displayName(),
                            building.position(),
                            frameType.displayName()
                        );

                        buildingsToUnregister.add(building);
                        continue;
                    }
                }
            }

*/