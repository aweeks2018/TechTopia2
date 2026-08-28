package com.techtopia2.data.village;

import java.util.Optional;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BuildingManager
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public void onStructureItemPlaced(
        ServerLevel level,
        Player player,
        ItemFrame frame)
    {
        ItemStack item = frame.getItem();

        if (item.isEmpty())
        {
            LOGGER.info("Frame was empty");
            return;
        }

        if (!StructureType.isStructureItem(item))
        {
            LOGGER.info("Item in frame was not a StructureType");
            return;
        }

        StructureType type =
            StructureType.getStructureTypeFromItem(item);

        if (!type.validator().isValid(level, frame))
        {
            LOGGER.info("Structure was not valid");
            return;
        }

        if(registerBuilding(frame.blockPosition(), type, player, level))
        {
            player.sendSystemMessage(Component.literal(type.displayName() + " structure registered to the village."));
        }
        else
        {
            LOGGER.info("Structure {} was NOT resgistered", type.displayName());
        }
    }
    
    public boolean registerBuilding(Building building, ServerLevel level)
    {
        VillageManager villageManager = new VillageManager();
        BlockPos position = building.position();
        StructureType type = building.type();

        if (type.equals(StructureType.TOWN_HALL))
        {
            return villageManager.registerTownHall(position, level);
        }
        else
        {
            VillageData data = VillageData.get(level);

            Optional<Village> village = villageManager.findVillage(position, level);
            if (village.isEmpty())
            {
                LOGGER.info("Could not find village");
                return false;
            }
            
            Village currentVillage = village.get();

            for (Building currentBuilding : currentVillage.buildings())
            {
                if (currentBuilding.position().equals(position))
                {
                    LOGGER.info("Building already registered here");

                    Player nearestPlayer = level.getNearestPlayer(
                            position.getX(), 
                            position.getY(), 
                            position.getZ(), 
                            1000.0D, 
                            entity -> true // This matches any player without extra conditions
                    );

                    if (nearestPlayer != null) 
                    {
                        nearestPlayer.sendSystemMessage(Component.literal(
                            "This building is already registered as another or the same building type")); 
                    } 
                    else 
                    {
                        LOGGER.info("This building is already registered as another or the same building type");
                    }
                    return false;
                }
            }
              
            Building buildingToAdd = new Building(position, type);
            currentVillage.buildings().add(buildingToAdd);

            // If the player is relocating a Town Hall then call the building agnistic serach to find a previosuly 
            updateBuildingMarker(level, position, true);

            data.unregisteredBuildings().removeIf(buildingToCheck -> buildingToCheck.position().equals(position));
            data.setDirty();
        }

        return true;
    }
    



    private boolean registerBuilding(BlockPos position, StructureType type, Player player, ServerLevel level)
    {
        VillageManager villageManager = new VillageManager();

        if (type.equals(StructureType.TOWN_HALL))
        {
            return villageManager.registerTownHall(position, level);
        }
        else
        {
            VillageData data = VillageData.get(level);

            Optional<Village> village = villageManager.findVillage(position, level);
            if (village.isEmpty())
            {
                LOGGER.info("Could not find village");
                return false;
            }

            Village currentVillage = village.get();

            for (Building building : currentVillage.buildings())
            {
                if (building.position().equals(position))
                {
                    ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                    if (item.isEmpty())
                    {
                        return false;
                    }
                    else if (StructureType.isStructureItem(item) && StructureType.getStructureTypeFromItem(item) == building.type() )
                    {
                        player.sendSystemMessage(Component.literal("Original building type marker restored, no new building registered"));
                    }
                    else
                    {
                        player.sendSystemMessage(Component.literal("This building is alreayd registered as a " + building.type() + " building. \nIf you want to unregister it, place the original item in the item frame \nand right click the item frame with a stick. "));
                    }
                    return false;
                }
            }

            Building buildingToAdd = new Building(position, type);
            currentVillage.buildings().add(buildingToAdd);

            // If the player is relocating a Town Hall then call the building agnistic serach to find a previosuly 
            updateBuildingMarker(level, position, true);


            data.unregisteredBuildings().removeIf(building -> building.position().equals(position));
            data.setDirty();
        }
        return true;
    }

    public Optional<StructureType> unregisterBuilding(BlockPos position, ServerLevel level)
    {
        VillageData data = VillageData.get(level);

        for (int index = 0; index < data.villages().size(); index++)
        {
            Village village = data.villages().get(index);
            if (village.townHall().position().equals(position))
            {
                for (Building building : village.buildings())
                {
                    updateBuildingMarker(level, building.position(), false);

                    data.addUnregisteredBuilding(building);
                    LOGGER.info("Adding {} ID: {} to the unregisteredBuildings", building.type().displayName(), building.type().id());
                }
                updateBuildingMarker(level, position, false);

                data.removeVillage(index);

                LOGGER.info("TOWN_HALL and village unregistered");
                return Optional.of(StructureType.TOWN_HALL);
            }
            
            LOGGER.info("not a townhall so lets see if its a building in the village ... ");
            Optional<Building> building = village.buildings().stream()
                    .filter(candidate -> candidate.position().equals(position))
                    .findFirst();
            if (building.isPresent())
            {
                village.buildings().remove(building.get());
                data.addUnregisteredBuilding(building.get());
                updateBuildingMarker(level, building.get().position(), false);

                LOGGER.info("{} building unregistered", building.get().type().displayName());
                return Optional.of(building.get().type());
            }
            LOGGER.info("not a buidling in the village ... ");
        }

        return Optional.empty();
    }

    protected void updateBuildingMarker(Level level, BlockPos position, boolean registeringNewBuidling)
    {
        for (ItemFrame itemFrame : level.getEntitiesOfClass(ItemFrame.class, new AABB(position)))
        {
            ItemStack itemInFrame = itemFrame.getItem();
            if (!itemInFrame.isEmpty() &&
                StructureType.isStructureItem(itemInFrame))
            {
                if(registeringNewBuidling)
                {
                    itemInFrame.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                }
                else // Unregistering a building ... 
                {
                    itemInFrame.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
                }

                // Re-set the item so the ItemFrame updates and syncs with the client
                itemFrame.setItem(itemInFrame);
            }
        }
    }

}
