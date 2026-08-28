package com.techtopia2;
import com.techtopia2.data.village.BuildingManager;
import com.techtopia2.data.village.StructureType;
import com.techtopia2.data.village.VillageData;
import com.techtopia2.entity.custom.FemaleNomadEntity;
import com.techtopia2.entity.custom.MaleNomadEntity;
import com.techtopia2.entity.custom.NomadEntity;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.Optional;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items; // Add this line at the top
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(TechTopia2.MOD_ID)
public final class TechTopia2
{
    public static final String MOD_ID = "techtopia2";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);

    public static final DeferredItem<Item> TOWN_HALL_ITEM = ITEMS.registerSimpleItem(
            "town_hall",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BARRACKS_ITEM = ITEMS.registerSimpleItem(
            "barracks",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BUTCHER_ITEM = ITEMS.registerSimpleItem(
            "butcher",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> GUARD_POST_ITEM = ITEMS.registerSimpleItem(
            "guard_post",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> HOMES_ITEM = ITEMS.registerSimpleItem(
            "homes",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> KITCHEN_ITEM = ITEMS.registerSimpleItem(
            "kitchen",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> LIBRARY_ITEM = ITEMS.registerSimpleItem(
            "library",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MERCHANT_STALL_ITEM = ITEMS.registerSimpleItem(
            "merchant_stall",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MINESHAFT_ITEM = ITEMS.registerSimpleItem(
            "mineshaft",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> RANCHER_PEN_ITEM = ITEMS.registerSimpleItem(
            "rancher_pen",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SCHOOL_ITEM = ITEMS.registerSimpleItem(
            "school",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SMITHY_ITEM = ITEMS.registerSimpleItem(
            "smithy",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> STORAGE_ITEM = ITEMS.registerSimpleItem(
            "storage",
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> TAVERN_ITEM = ITEMS.registerSimpleItem(
            "tavern",
            () -> new Item.Properties().stacksTo(1));
    
    public static final DeferredHolder<EntityType<?>, EntityType<MaleNomadEntity>> MALE_NOMAD =
        ENTITIES.registerEntityType("male_nomad", MaleNomadEntity::new, MobCategory.CREATURE, builder ->
            builder.sized(0.6F, 1.8F));

    public static final DeferredHolder<EntityType<?>, EntityType<FemaleNomadEntity>> FEMALE_NOMAD =
        ENTITIES.registerEntityType("female_nomad", FemaleNomadEntity::new, MobCategory.CREATURE, builder ->
            builder.sized(0.6F, 1.8F));

    public TechTopia2(IEventBus modEventBus)
    {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::createAttributes);
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("TechTopia2 loaded");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        // 1. delete_village command
        event.getDispatcher().register(Commands.literal("techtopia")
                .then(Commands.literal("delete_village")
                        .executes(context ->
                        {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel level = (ServerLevel)context.getSource().getLevel();
                            VillageData villageData = VillageData.get(level);
                            if (villageData.forceDeleteAllVillages(level))
                            {
                                context.getSource().sendSuccess(
                                        () -> Component.literal("Village deleted."), true);
                                return 1;
                            }

                            context.getSource().sendFailure(Component.literal(
                                    "You are not within the bounds of a registered village."));
                            return 0;
                        })));
    }

    private void createAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)
    {
        event.put(MALE_NOMAD.get(), NomadEntity.createAttributes().build());
        event.put(FEMALE_NOMAD.get(), NomadEntity.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(TOWN_HALL_ITEM);
            event.accept(BARRACKS_ITEM);
            event.accept(BUTCHER_ITEM);
            event.accept(GUARD_POST_ITEM);
            event.accept(HOMES_ITEM);
            event.accept(KITCHEN_ITEM);
            event.accept(LIBRARY_ITEM);
            event.accept(MERCHANT_STALL_ITEM);
            event.accept(MINESHAFT_ITEM);
            event.accept(RANCHER_PEN_ITEM);
            event.accept(SCHOOL_ITEM);
            event.accept(SMITHY_ITEM);
            event.accept(STORAGE_ITEM);
            event.accept(TAVERN_ITEM);
        }
    }

    @SubscribeEvent
    public void onItemFrameInteract(PlayerInteractEvent.EntityInteract event)
    {
        // Ignore the client-side event.
        if (!(event.getLevel() instanceof ServerLevel level))
        {
            LOGGER.info("Bad level");
            return;
        }

        // Only process ItemFrames.
        if (!(event.getTarget() instanceof ItemFrame frame))
        {
            LOGGER.info("Bad frame");
            return;
        }

        Player player = event.getEntity();

        /* Now check the players had for a stick and if it is a stick, rightclicking the item 
           frame will remove the strcutre from the village */
        if (player.getMainHandItem().is(Items.STICK))
        {
            VillageData data = VillageData.get(level);
            Optional<StructureType> type = data.unregisterStructure(frame.blockPosition(), event);

            if (type.isPresent())
            {
                if (type.get().equals(StructureType.TOWN_HALL))
                {
                    player.sendSystemMessage(Component.literal(
                        "Town Hall at " + frame.blockPosition().getX() + ", "
                            + frame.blockPosition().getY() + ", "
                            + frame.blockPosition().getZ()
                            + " has been unregistered."));
                }
                else
                {
                    player.sendSystemMessage(Component.literal(
                        type.get().displayName().substring(0, 1).toUpperCase()
                            + type.get().displayName().substring(1)
                            + " at " + frame.blockPosition().getX() + ", "
                            + frame.blockPosition().getY() + ", "
                            + frame.blockPosition().getZ()
                            + " has been unregistered from the village."));
                }
            } 

            return;
        }
 
    }

}