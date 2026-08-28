package com.techtopia2.data.village;

import com.mojang.serialization.Codec;
import com.techtopia2.items.TechTopiaItems;
import com.techtopia2.items.stuctures.validators.BarracksValidator;
import com.techtopia2.items.stuctures.validators.ButcherValidator;
import com.techtopia2.items.stuctures.validators.GeneralValidator;
import com.techtopia2.items.stuctures.validators.GuardPostValidator;
import com.techtopia2.items.stuctures.validators.HomeValidator;
import com.techtopia2.items.stuctures.validators.KitchenValidator;
import com.techtopia2.items.stuctures.validators.RoomValidator;
import com.techtopia2.items.stuctures.validators.TownHallValidator;

import net.minecraft.world.item.ItemStack;

public enum StructureType
{
    TOWN_HALL("town_hall", "Town Hall", TownHallValidator.INSTANCE),
    BARRACKS("barracks", "Barracks", BarracksValidator.INSTANCE),
    BUTCHER("butcher", "Butcher House", ButcherValidator.INSTANCE),
    GUARD_POST("guard_post", "Guard Post", GuardPostValidator.INSTANCE),
    HOMES("homes", "Homes", HomeValidator.INSTANCE),
    KITCHEN("kitchen", "Kitchen", KitchenValidator.INSTANCE),
    LIBRARY("library", "Library", GeneralValidator.INSTANCE),
    MERCHANT_STALL("merchant_stall", "Merchant Stall", GeneralValidator.INSTANCE),
    MINESHAFT("mineshaft", "Mineshaft", GeneralValidator.INSTANCE),
    RANCHER_PEN("rancher_pen", "Rancher Pen", GeneralValidator.INSTANCE),
    SCHOOL("school", "School", GeneralValidator.INSTANCE),
    SMITHY("smithy", "Smithy", GeneralValidator.INSTANCE),
    STORAGE("storage", "Storage", GeneralValidator.INSTANCE),
    TAVERN("tavern", "Tavern", GeneralValidator.INSTANCE);

    public static final Codec<StructureType> CODEC =
        Codec.STRING.xmap(
                StructureType::fromId,
                StructureType::id
        );

    private final String id;
    private final String displayName;
    private final RoomValidator validator;

    StructureType(String id, String displayName, RoomValidator validator)
    {
        this.id = id;
        this.displayName = displayName;
        this.validator = validator;
    }

    public String id()
    {
        return id;
    }

    public String displayName()
    {
        return displayName;
    }

    public RoomValidator validator()
    {
        return validator;
    }

    public static StructureType fromId(String id)
    {
        for (StructureType type : values())
        {
            if (type.id.equals(id))
            {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown structure type: " + id);
    }
    
    public static boolean isStructureItem(ItemStack stack)
    {
        return stack.is(TechTopiaItems.TOWN_HALL_ITEM.get())
                || stack.is(TechTopiaItems.BARRACKS_ITEM.get())
                || stack.is(TechTopiaItems.BUTCHER_ITEM.get())
                || stack.is(TechTopiaItems.GUARD_POST_ITEM.get())
                || stack.is(TechTopiaItems.HOMES_ITEM.get())
                || stack.is(TechTopiaItems.KITCHEN_ITEM.get())
                || stack.is(TechTopiaItems.LIBRARY_ITEM.get())
                || stack.is(TechTopiaItems.MERCHANT_STALL_ITEM.get())
                || stack.is(TechTopiaItems.MINESHAFT_ITEM.get())
                || stack.is(TechTopiaItems.RANCHER_PEN_ITEM.get())
                || stack.is(TechTopiaItems.SCHOOL_ITEM.get())
                || stack.is(TechTopiaItems.SMITHY_ITEM.get())
                || stack.is(TechTopiaItems.STORAGE_ITEM.get())
                || stack.is(TechTopiaItems.TAVERN_ITEM.get());
    }

    public static StructureType getStructureTypeFromItem(ItemStack stack)
    {
        return  stack.is(TechTopiaItems.TOWN_HALL_ITEM.get()) ? StructureType.TOWN_HALL :
                stack.is(TechTopiaItems.BARRACKS_ITEM.get()) ? StructureType.BARRACKS : 
                stack.is(TechTopiaItems.BUTCHER_ITEM.get()) ? StructureType.BUTCHER :
                stack.is(TechTopiaItems.GUARD_POST_ITEM.get()) ? StructureType.GUARD_POST :
                stack.is(TechTopiaItems.HOMES_ITEM.get()) ? StructureType.HOMES :
                stack.is(TechTopiaItems.KITCHEN_ITEM.get()) ? StructureType.KITCHEN :
                stack.is(TechTopiaItems.LIBRARY_ITEM.get()) ? StructureType.LIBRARY :
                stack.is(TechTopiaItems.MERCHANT_STALL_ITEM.get()) ? StructureType.MERCHANT_STALL :
                stack.is(TechTopiaItems.MINESHAFT_ITEM.get()) ? StructureType.MINESHAFT :
                stack.is(TechTopiaItems.RANCHER_PEN_ITEM.get()) ? StructureType.RANCHER_PEN :
                stack.is(TechTopiaItems.SCHOOL_ITEM.get()) ? StructureType.SCHOOL :
                stack.is(TechTopiaItems.SMITHY_ITEM.get()) ? StructureType.SMITHY :
                stack.is(TechTopiaItems.STORAGE_ITEM.get()) ? StructureType.STORAGE :
                stack.is(TechTopiaItems.TAVERN_ITEM.get()) ? StructureType.TAVERN 
                /* defualt */               : null;
    }
}
