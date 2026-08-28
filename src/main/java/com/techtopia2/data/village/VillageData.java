package com.techtopia2.data.village;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.techtopia2.TechTopia2;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.server.level.ServerLevel;

public final class VillageData extends SavedData
{
    public static final Logger LOGGER = LogUtils.getLogger();

    private final List<Village> villages;
    private final List<Building> unregisteredBuildings;

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

    public List<Village> villages()
    {
        return villages;
    }

    public List<Building> unregisteredBuildings()
    {
        return unregisteredBuildings;
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

    public void addUnregisteredBuilding(Building building)
    {
        unregisteredBuildings.add(building);
        setDirty();
    }

    public void removeUnregisteredBuilding(int buildingIndex)
    {
        unregisteredBuildings.remove(buildingIndex);
        setDirty();
    }

    
}