package com.techtopia2.data.village;

import org.slf4j.Logger;

import java.util.ArrayList;
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
            Codec.STRING.fieldOf("type").forGetter(Building::type))
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
        //this(new ArrayList<>(), new ArrayList<>());
        this.villages = new ArrayList<>();
        this.unregisteredBuildings = new ArrayList<>();
        System.out.println("--- NEW WORLD FACTORY INITIALIZED (EMPTY STATE) ---");
    }

    private VillageData(List<Village> villages, List<Building> unregisteredBuildings)
    {
        this.villages = new ArrayList<>(villages);
        this.unregisteredBuildings = new ArrayList<>(unregisteredBuildings);

        LOGGER.info("--- CODEC LOADED DATA ---");
        LOGGER.info("Villages count parsed from file: " + villages.size());


        LOGGER.info("VillageData Initialized! Total Saved Villages Loaded: " + this.villages.size());
        for (Village v : this.villages) {
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

    private boolean hasItemFrame(Level level, BlockPos pos) 
    {
        // Creates a bounding box covering the specific block position
        AABB box = new AABB(pos);
        
        // Check if any ItemFrame exists within that block space
        return !level.getEntitiesOfClass(ItemFrame.class, box).isEmpty();
    }
    
    public void makeEnchanted(Level level, boolean isEnchanted, Building building, TechTopia2 techTopia2)
    {
        if(hasItemFrame(level, building.position()))
        {        
            LOGGER.debug("makeEnchanted......foundItem Frame");

            AABB box = new AABB(building.position());
            for (ItemFrame itemFrame : level.getEntitiesOfClass(ItemFrame.class, box))
            {
                if(!itemFrame.getItem().isEmpty() &&
                   techTopia2.isStructureItem(itemFrame.getItem()))
                {
                    LOGGER.debug("makeEnchanted......!itemFrame.getItem().isEmpty()");
                    ItemStack itemInFrame = itemFrame.getItem();
                    if (isEnchanted) 
                    {
                        // In 1.20.5+, use Data Components to explicitly enable the glint
                        // If on an older version (1.20.4 or below), use itemInFrame.getOrCreateTag().putBoolean("Enchantments", true);
                        itemInFrame.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                        LOGGER.debug("makeEnchantedLong......isEnchanted");
                    } 
                    else 
                    {
                        itemInFrame.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
                        LOGGER.debug("makeEnchantedLong......isEnchanted");
                    }
                    
                    // Re-set the item so the ItemFrame updates and syncs with the client
                    itemFrame.setItem(itemInFrame);
                }
            }
        }
    }

    private void makeEnchanted(PlayerInteractEvent.EntityInteract event, boolean isEnchanted)
    {
        if (event.getTarget() instanceof ItemFrame itemFrame) 
        {
            Player player = event.getEntity();
            ItemStack item = player.getMainHandItem().copy();
            LOGGER.debug("makeEnchanted ");

            if (!item.isEmpty()) 
            {
                LOGGER.debug("makeEnchanted: !item.isEmpty()");

                if(!item.is(Items.STICK))
                {
                    if(isEnchanted)
                    {
                        item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true); 
                        LOGGER.debug("makeEnchanted: isEnchanted");
                    }
                    else
                    {
                        item.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
                        LOGGER.debug("makeEnchanted: !isEnchanted");
                    }
                    
                    // Re-set the item so the ItemFrame updates and syncs with the client
                    itemFrame.setItem(item);
                }
                else
                {
                    ItemStack itemInFrame = itemFrame.getItem();
                    LOGGER.debug("STICK itemInFrame ");
                    if(!itemInFrame.isEmpty())
                    {
                        itemInFrame.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
                       
                        // Re-set the item so the ItemFrame updates and syncs with the client
                        itemFrame.setItem(itemInFrame);
                    }
                }
            }
            else
            {
                LOGGER.debug("itemCopy......isEmpty()");
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

    public boolean registerTownHall(BlockPos position, PlayerInteractEvent.EntityInteract event)
    {
        if (findVillage(position, (ServerLevel)event.getLevel()).isPresent())
        {
            return false;
        }

        makeEnchanted(event, true);

        Building newTownHall = new Building(position, "town_hall");
        Village newVillage = new Village(newTownHall, new ArrayList<>());
        VillageData.get((ServerLevel) event.getLevel()).addVillage(newVillage);
        
        return true;
    }

    public boolean registerBuilding(BlockPos position, String type, PlayerInteractEvent.EntityInteract event, TechTopia2 techTopia2)
    {
        Optional<Village> village = findVillage(position, (ServerLevel) event.getLevel());
        if (village.isEmpty())
        {
            return false;
        }

        Player player = event.getEntity();
        Village currentVillage = village.get();

        for (Building building : currentVillage.buildings())
        {
            if (building.position().equals(position))
            {
                ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (!item.isEmpty() && techTopia2.isStructureItem(item) && techTopia2.structureType(item) == building.type() )
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

        LOGGER.debug("registerBuilding......noyt stream()");

        // If the player is relocating a Town Hall then call the building agnistic serach to find a previosuly 
        if(player.getItemInHand(InteractionHand.MAIN_HAND).is(TechTopia2.TOWN_HALL_ITEM))
        {
            makeEnchanted(event.getLevel(), true, buildingToAdd, techTopia2);
        }
        else
        {
            makeEnchanted(event, true);
        }

        unregisteredBuildings.removeIf(building -> building.position().equals(position));
        setDirty();
        return true;
    }

    public Optional<String> unregisterStructure(BlockPos position, PlayerInteractEvent.EntityInteract event, TechTopia2 techTopia2)
    {
        for (int index = 0; index < VillageData.get((ServerLevel)event.getLevel()).villages.size(); index++)
        {
            Village village = VillageData.get((ServerLevel)event.getLevel()).villages.get(index);
            if (village.townHall().position().equals(position))
            {
                unregisteredBuildings.addAll(village.buildings());
                for (Building building : village.buildings())
                {
                    makeEnchanted(event.getLevel(), false, building, techTopia2);
                }
                makeEnchanted(event, false);

                VillageData.get((ServerLevel)event.getLevel()).removeVillage(index);

                return Optional.of("town_hall");
            }
            
            Optional<Building> building = village.buildings().stream()
                    .filter(candidate -> candidate.position().equals(position))
                    .findFirst();
            if (building.isPresent())
            {
                village.buildings().remove(building.get());
                unregisteredBuildings.add(building.get());
                makeEnchanted(event, false);
                setDirty();
                return Optional.of(building.get().type());
            }
        }
        return Optional.empty();
    }


    public boolean forceDeleteAllVillages(Level level, TechTopia2 techTopia2) 
    {
        // 1. Loop through and clear any block states / visual data if needed
        for (Village village : this.villages) 
        {
            makeEnchanted(level, false, village.townHall(), techTopia2);
            for (Building b : village.buildings()) 
            {
                makeEnchanted(level, false, b, techTopia2);
            }
        }
        
        // 2. Completely dump out the data pools
        this.villages.clear();
        this.unregisteredBuildings.clear();
        
        // 3. Force save the cleared empty lists to the hard drive immediately
        this.setDirty();
        LOGGER.info("!!! DANGER !!! All villages have been forcefully purged from the database.");
        return true;
    }

    /*
    public boolean deleteVillageContaining(BlockPos position, Level level, TechTopia2 techTopia2)
    {
        
        for (int index = 0; index < VillageData.get((ServerLevel)level).villages.size(); index++)
        {
            Village village = VillageData.get((ServerLevel)level).villages.get(index);
            if (village.townHall().position().distSqr(position)
                    <= (long) VILLAGE_RADIUS * VILLAGE_RADIUS)
            {
                for (Building building : village.buildings())
                {
                    makeEnchanted(level, false, building, techTopia2);
                }

                makeEnchanted(level, false, village.townHall(), techTopia2);

                unregisteredBuildings.addAll(village.buildings());

                removeVillage((ServerLevel)level, index);

                unregisteredBuildings.clear(); // This should only happen with the special command /delete_village
                
                return true;
            }
        }
        return false;
    }

     */

    public List<Building> getUnregisteredBuildings()
    {
        return List.copyOf(unregisteredBuildings);
    }

    public void removeUnregisteredBuilding(Building building)
    {
        if (unregisteredBuildings.remove(building))
        {
            setDirty();
        }
    }

    public static boolean isWithinVillage(ServerLevel level, BlockPos position)
    {
        return level.getServer().getDataStorage()
                .computeIfAbsent(TYPE)
                .findVillage(position, level)
                .isPresent();
    }

    // public Optional<Village> findVillage(BlockPos position, ServerLevel level)
    // {
    //     long radiusSquared = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;

    //     for (Village village : villages)
    //     {
    //         LOGGER.info("Village at: " + village.townHall().position().toString());
    //     }

    //     return villages.stream()
    //             .filter(village -> village.townHall().position().distSqr(position) <= radiusSquared)
    //             .findFirst();
    // }

    // public Optional<Village> findVillage(Vec3i position, ServerLevel level)
    // {
    //     long radiusSquared = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;

    //     for (Village village : villages)
    //     {
    //         LOGGER.info("Village at: " + village.townHall().position().toString());
    //     }

    //     return villages.stream()
    //             .filter(village -> village.townHall().position().distSqr(position) <= radiusSquared)
    //             .findFirst();
    // }


    public Optional<Village> findVillage(BlockPos position, ServerLevel level)
    {
        // Force long casting to completely prevent any math integer overflows
        long radiusSquared = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;

        return villages.stream()
                .filter(village -> {
                    BlockPos hallPos = village.townHall().position();
                    
                    // 1. Calculate the flat 2D distance offsets as long values
                    long dx = (long) hallPos.getX() - position.getX();
                    long dz = (long) hallPos.getZ() - position.getZ();
                    
                    // 2. Compute the 2D squared distance
                    long distance2D = (dx * dx) + (dz * dz);
                    
                    // 3. Return true if the entity is inside the 2D flat circle boundary
                    return distance2D <= radiusSquared;
                })
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