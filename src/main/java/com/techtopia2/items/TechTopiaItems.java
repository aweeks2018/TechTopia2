package com.techtopia2.items;

import com.techtopia2.TechTopia2;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TechTopiaItems
{
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TechTopia2.MOD_ID);

    public static final DeferredItem<Item> TOWN_HALL_ITEM =
            ITEMS.registerSimpleItem(
                    "town_hall",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> BARRACKS_ITEM =
            ITEMS.registerSimpleItem(
                    "barracks",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> BUTCHER_ITEM =
            ITEMS.registerSimpleItem(
                    "butcher",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> GUARD_POST_ITEM =
            ITEMS.registerSimpleItem(
                    "guard_post",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> HOMES_ITEM =
            ITEMS.registerSimpleItem(
                    "homes",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> KITCHEN_ITEM =
            ITEMS.registerSimpleItem(
                    "kitchen",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> LIBRARY_ITEM =
            ITEMS.registerSimpleItem(
                    "library",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> MERCHANT_STALL_ITEM =
            ITEMS.registerSimpleItem(
                    "merchant_stall",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> MINESHAFT_ITEM =
            ITEMS.registerSimpleItem(
                    "mineshaft",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> RANCHER_PEN_ITEM =
            ITEMS.registerSimpleItem(
                    "rancher_pen",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> SCHOOL_ITEM =
            ITEMS.registerSimpleItem(
                    "school",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> SMITHY_ITEM =
            ITEMS.registerSimpleItem(
                    "smithy",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> STORAGE_ITEM =
            ITEMS.registerSimpleItem(
                    "storage",
                    () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> TAVERN_ITEM =
            ITEMS.registerSimpleItem(
                    "tavern",
                    () -> new Item.Properties().stacksTo(1));
}