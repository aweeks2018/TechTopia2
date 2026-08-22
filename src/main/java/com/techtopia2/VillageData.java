package com.techtopia2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class VillageData extends SavedData
{
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
            Codec.list(VILLAGE_CODEC).fieldOf("villages").forGetter(data -> data.villages))
            .apply(instance, VillageData::new));

    public static final SavedDataType<VillageData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TechTopia2.MOD_ID, "villages"),
            VillageData::new,
            CODEC,
            null);

    private final List<Village> villages;

    public VillageData()
    {
        this(new ArrayList<>());
    }

    private VillageData(List<Village> villages)
    {
        this.villages = new ArrayList<>(villages);
    }

    public boolean registerTownHall(BlockPos position)
    {
        if (findVillage(position).isPresent())
        {
            return false;
        }
        villages.add(new Village(position, new ArrayList<>()));
        setDirty();
        return true;
    }

    public boolean registerBuilding(BlockPos position, String type)
    {
        Optional<Village> village = findVillage(position);
        if (village.isEmpty())
        {
            return false;
        }
        Village target = village.get();
        if (target.buildings().stream().anyMatch(building -> building.position().equals(position)))
        {
            return true;
        }
        target.buildings().add(new Building(position, type));
        setDirty();
        return true;
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