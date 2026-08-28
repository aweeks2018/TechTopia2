package com.techtopia2.data.village;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.techtopia2.TechTopia2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.server.level.ServerLevel;

public final class VillageData extends SavedData
{
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final int VILLAGE_RADIUS = 1000;

    private static final Codec<Building> BUILDING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("position").forGetter(Building::position),
            StructureType.CODEC.fieldOf("type").forGetter(Building::type))
            .apply(instance, Building::new));

    private static final Codec<Village> VILLAGE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BUILDING_CODEC.fieldOf("townHall").forGetter(Village::townHall),
            Codec.list(BUILDING_CODEC).fieldOf("buildings").forGetter(Village::buildings))
            .apply(instance, Village::new));
            
    private static final Codec<VillageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(VILLAGE_CODEC).fieldOf("villages").forGetter(data -> data.villages),
            Codec.list(BUILDING_CODEC).fieldOf("unregisteredBuildings").forGetter(data -> data.unregisteredBuildings))
            .apply(instance, VillageData::new));

    public static final SavedDataType<VillageData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TechTopia2.MOD_ID, "villages"),
            VillageData::new,
            CODEC,
            null);

    private final List<Village> villages;
    private final List<Building> unregisteredBuildings;

    public VillageData()
    {
        this.villages = new ArrayList<>();
        this.unregisteredBuildings = new ArrayList<>();

        LOGGER.info("--- NEW WORLD FACTORY INITIALIZED (EMPTY STATE) ---");
    }

    private VillageData(List<Village> villages, List<Building> unregisteredBuildings)
    {
        this.villages = new ArrayList<>(villages);
        this.unregisteredBuildings = new ArrayList<>(unregisteredBuildings);

        /* All of tyhe code below is just for logging */
        LOGGER.info("--- CODEC LOADED DATA ---");
        LOGGER.info("Villages count parsed from file: " + villages.size());
        LOGGER.info("VillageData Initialized! Total Saved Villages Loaded: " + this.villages.size());

        for (Village v : this.villages) 
        {
            LOGGER.info(" -> Loaded Village at Town Hall: " + v.townHall().position().toString());
        }
    }
    
    public static VillageData get(ServerLevel level)
    {
        // Force it to query the master server's data storage wrapper 
        // to prevent dim/level instance fragmentation
        return level.getServer()
                    .overworld()
                    .getDataStorage()
                    .computeIfAbsent(TYPE);
    }
    
    public void updateBuildingMarker(Level level, BlockPos position, boolean registeringNewBuidling)
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

    public void addVillage(Village village)
    {
        villages.add(village);
        setDirty();
    }
    
    public void removeVillage(int villageIndex)
    {
        villages.remove(villageIndex);
        setDirty();
    }

    public boolean registerTownHall(BlockPos position, ServerLevel level)
    {
        VillageData data = get(level);

        if (data.findVillage(position, level).isPresent())
        {
            LOGGER.info("registerTownHall: Already inside a village");
            return false;
        }

        updateBuildingMarker(level, position, true);
 
        Building newTownHall = new Building(position, StructureType.TOWN_HALL);
        Village newVillage = new Village(newTownHall, new ArrayList<>());

        Iterator<Building> iterator = data.unregisteredBuildings.iterator();

        while (iterator.hasNext())
        {
            Building building = iterator.next();

            if (VillageData.isWithinVillage(building.position(), level, newVillage))
            {
                updateBuildingMarker(level, building.position(), true);

                LOGGER.info("Adding {} building to new village", building.type().displayName());

                newVillage.buildings().add(building);
                iterator.remove();
            }
        }
       
        data.addVillage(newVillage);

        return true;
    }

    public boolean registerBuilding(BlockPos position, StructureType type, Player player, ServerLevel level)
    {
        if (type.equals(StructureType.TOWN_HALL))
        {
            return registerTownHall(position, level);
        }
        else
        {
            VillageData data = get(level);

            Optional<Village> village = data.findVillage(position, level);
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
                    if (!item.isEmpty() && StructureType.isStructureItem(item) && StructureType.getStructureTypeFromItem(item) == building.type() )
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


            data.unregisteredBuildings.removeIf(building -> building.position().equals(position));
            setDirty();
        }
        return true;
    }

    public Optional<StructureType> unregisterStructure(BlockPos position, PlayerInteractEvent.EntityInteract event)
    {
        VillageData data = get((ServerLevel)event.getLevel());

        for (int index = 0; index < data.villages.size(); index++)
        {
            Village village = data.villages.get(index);
            if (village.townHall().position().equals(position))
            {
                for (Building building : village.buildings())
                {
                    updateBuildingMarker(event.getLevel(), building.position(), false);

                    data.unregisteredBuildings.add(building);
                    LOGGER.info("Adding {} ID: {} to the unregisteredBuildings", building.type().displayName(), building.type().id());
                }
                updateBuildingMarker(event.getLevel(), position, false);

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
                data.unregisteredBuildings.add(building.get());
                updateBuildingMarker(event.getLevel(), building.get().position(), false);

                setDirty();
                LOGGER.info("{} building unregistered", building.get().type().displayName());
                return Optional.of(building.get().type());
            }
            LOGGER.info("not a buidling in the village ... ");
        }

        return Optional.empty();
    }


    public boolean forceDeleteAllVillages(Level level) 
    {
        VillageData data = get((ServerLevel)level);
        // 1. Loop through and clear any block states / visual data if needed
        for (Village village : data.villages) 
        {
            updateBuildingMarker(level, village.townHall().position(), false);
            for (Building building : village.buildings()) 
            {
                updateBuildingMarker(level, building.position(), false);
            }
        }
        
        // 2. Completely dump out the data pools
        data.villages.clear();
        data.unregisteredBuildings.clear();
        
        // 3. Force save the cleared empty lists to the hard drive immediately
        setDirty();
        LOGGER.info("!!! DANGER !!! All villages have been forcefully purged from the database.");
        return true;
    }

    public static boolean isWithinAnyVillage(ServerLevel level, BlockPos position)
    {
        VillageData data = get(level);
        return data.findVillage(position, level)
                .isPresent();
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
        return villages.stream()
                .filter(village -> isWithinVillage(position, level, village))
                .findFirst();
    }

    public Optional<Village> findVillage(Vec3i position, ServerLevel level)
    {
        // Convert the incoming Vec3i directly into a standard BlockPos 
        // to utilize Minecraft's built-in spatial boundaries properly
        BlockPos blockPos = new BlockPos(position.getX(), position.getY(), position.getZ());
        return findVillage(blockPos, level);
    }

}