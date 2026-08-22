package com.techtopia2;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
            BlockPos.CODEC.fieldOf("townHall").forGetter(Village::townHall),
            Codec.list(BUILDING_CODEC).fieldOf("buildings").forGetter(Village::buildings))
            .apply(instance, Village::new));
            
    private static final Codec<VillageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(VILLAGE_CODEC).fieldOf("villages").forGetter(data -> data.villages),
                Codec.list(BUILDING_CODEC).optionalFieldOf("unregisteredBuildings", List.of())
                .forGetter(data -> data.unregisteredBuildings))
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
        this(new ArrayList<>(), new ArrayList<>());
    }

    private VillageData(List<Village> villages, List<Building> unregisteredBuildings)
    {
        this.villages = new ArrayList<>(villages);
        this.unregisteredBuildings = new ArrayList<>(unregisteredBuildings);
    }

    private boolean hasItemFrame(Level level, BlockPos pos) 
    {
        // Creates a bounding box covering the specific block position
        AABB box = new AABB(pos);
        
        // Check if any ItemFrame exists within that block space
        return !level.getEntitiesOfClass(ItemFrame.class, box).isEmpty();
    }
    
    public void makeEnchanted(PlayerInteractEvent.EntityInteract event, boolean isEnchanted, Building building, TechTopia2 techTopia2)
    {
        if(hasItemFrame(event.getLevel(), building.position))
        {        
            
            LOGGER.debug("makeEnchanted......foundItem Frame");

            AABB box = new AABB(building.position);
            for (ItemFrame itemFrame : event.getLevel().getEntitiesOfClass(ItemFrame.class, box))
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

    public boolean registerTownHall(BlockPos position, PlayerInteractEvent.EntityInteract event)
    {
        if (findVillage(position).isPresent())
        {
            return false;
        }
        villages.add(new Village(position, new ArrayList<>()));
        makeEnchanted(event, true);
        
        setDirty();
        return true;    
    }

    public boolean registerBuilding(BlockPos position, String type, PlayerInteractEvent.EntityInteract event, TechTopia2 techTopia2)
    {
        Optional<Village> village = findVillage(position);
        if (village.isEmpty())
        {
            return false;
        }

        Player player = event.getEntity();
        Village target = village.get();
        if (target.buildings().stream().anyMatch(building -> building.position().equals(position)))
        {
            LOGGER.debug("registerBuilding......stream()");
            return true;
        }
        Building buildingToAdd = new Building(position, type);
        target.buildings().add(buildingToAdd);

        LOGGER.debug("registerBuilding......noyt stream()");

        // If the player is relocating a Town Hall then call the building agnistic serach to find a previosuly 
        if(player.getItemInHand(InteractionHand.MAIN_HAND).is(techTopia2.TOWN_HALL_ITEM))
        {
            makeEnchanted(event, true, buildingToAdd, techTopia2);
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
        for (int index = 0; index < villages.size(); index++)
        {
            Village village = villages.get(index);
            if (village.townHall().equals(position))
            {
                unregisteredBuildings.addAll(village.buildings());
                for (Building building : village.buildings())
                {
                    makeEnchanted(event, false, building, techTopia2);
                }
                makeEnchanted(event, false);
                villages.remove(index);
                setDirty();
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

    public boolean deleteVillageContaining(BlockPos position)
    {
        for (int index = 0; index < villages.size(); index++)
        {
            Village village = villages.get(index);
            if (village.townHall().distSqr(position)
                    <= (long) VILLAGE_RADIUS * VILLAGE_RADIUS)
            {
                unregisteredBuildings.addAll(village.buildings());
                villages.remove(index);
                unregisteredBuildings.clear(); // This should only happen with the special command /delete_village
                setDirty();
                return true;
            }
        }
        return false;
    }

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
                .findVillage(position)
                .isPresent();
    }

    private Optional<Village> findVillage(BlockPos position)
    {
        long radiusSquared = (long) VILLAGE_RADIUS * VILLAGE_RADIUS;
        return villages.stream()
                .filter(village -> village.townHall().distSqr(position) <= radiusSquared)
                .findFirst();
    }

    public record Building(BlockPos position, String type)
    {
    }

    public record Village(BlockPos townHall, List<Building> buildings)
    {
    }
}