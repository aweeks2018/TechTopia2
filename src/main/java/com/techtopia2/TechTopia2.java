package com.techtopia2;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.util.Optional;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items; // Add this line at the top
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.core.BlockPos;

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
                            VillageData villageData = getVillageData(player.level());
                            if (villageData.deleteVillageContaining(player.blockPosition()))
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
        boolean registeredBuilding = false; 
        // Structure markers activate when a player places them into an item frame.
        // Only the server should validate and register the structure.
        if (event.getEntity().level().isClientSide()
                || !(event.getTarget() instanceof ItemFrame)
                || !isStructureItem(event.getItemStack()))
        {
            return;
        }

        Player player = event.getEntity();
        ItemFrame frame = (ItemFrame) event.getTarget();

        // Town Hall creates a village; every other marker registers with an existing one.
        if (event.getItemStack().is(GUARD_POST_ITEM.get())
            && GuardPostValidator.INSTANCE.isValid(player.level(), frame))
        {
            registeredBuilding = registerBuilding(player, frame, "guard_post", "Guard Post", event);
        }
        else if (event.getItemStack().is(TOWN_HALL_ITEM.get())
            && TownHallValidator.INSTANCE.isValid(player.level(), frame))
        {
            VillageData villageData = getVillageData((ServerLevel) player.level());
            if (villageData.registerTownHall(frame.blockPosition(), event))
            {
                reevaluateUnregisteredBuildings((ServerLevel) player.level(), event);
                player.sendSystemMessage(Component.literal(
                    "Town Hall recognized. A new village was registered with a "
                        + VillageData.VILLAGE_RADIUS + "-block radius."));
                registeredBuilding = true; 
            }
            else
            {
                player.sendSystemMessage(Component.literal(
                        "Town Hall recognized. It is already inside a registered village."));
            }
        }
        else if ((event.getItemStack().is(BARRACKS_ITEM.get())
                  && BarracksValidator.INSTANCE.isValid(player.level(), frame)) ||
                 (event.getItemStack().is(BUTCHER_ITEM.get())
                  && ButcherValidator.INSTANCE.isValid(player.level(), frame)) ||
                 (event.getItemStack().is(GUARD_POST_ITEM.get())
                  && GuardPostValidator.INSTANCE.isValid(player.level(), frame)))
        {
            registeredBuilding = registerBuilding(player, frame, structureType(event.getItemStack()), structureName(event.getItemStack()), event);
        }
        else if (event.getItemStack().is(BARRACKS_ITEM.get()))
        {
                player.sendSystemMessage(Component.literal(
                       structureName(event.getItemStack()) + " needs 4 floor spaces, a 2-30 block height, a roof, and a door."));
        }
        else if (event.getItemStack().is(BUTCHER_ITEM.get()))
        {
                player.sendSystemMessage(Component.literal(
                    "Butcher needs 4 floor spaces, a 2-30 block height, a roof, and a door."));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    "Town Hall needs 4 floor spaces, a 2-30 block height, a roof, and a door."));
        }

        if (registeredBuilding)
        {
            // clear hand 
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }        
    }

    private boolean registerBuilding(Player player, ItemFrame frame, String type, String displayName, PlayerInteractEvent.EntityInteract event)
    {
        VillageData villageData = getVillageData((ServerLevel) player.level());
        if (villageData.registerBuilding(frame.blockPosition(), type, event, this))
        {
            player.sendSystemMessage(Component.literal(displayName + " structure registered to the village."));
            return true;
        }
        else
        {
            return false;
        }
    }

    private VillageData getVillageData(ServerLevel level)
    {
        return level.getServer().getDataStorage().computeIfAbsent(VillageData.TYPE);
    }


    @SubscribeEvent
    public void onPlayerRightClickEntityWithDebugTool(PlayerInteractEvent.EntityInteract event)
    {
        Player player = event.getEntity();

        /* base case we know we are not interacrting with the item we wnat  */
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getTarget() instanceof ItemFrame frame))
        {
            return;
        }

        /* Now check the players had for a stick and if it is a stick, rightclicking the item 
           frame will remove the strcutre from the village */
        if (player.getMainHandItem().is(Items.STICK))
        {
            VillageData villageData = getVillageData(level);
            Optional<String> structureType = villageData.unregisterStructure(frame.blockPosition(), event, this);

            if (structureType.isPresent())
            {
                if (structureType.get().equals("town_hall"))
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
                        structureType.get().substring(0, 1).toUpperCase()
                            + structureType.get().substring(1)
                            + " at " + frame.blockPosition().getX() + ", "
                            + frame.blockPosition().getY() + ", "
                            + frame.blockPosition().getZ()
                            + " has been unregistered from the village."));
                }
            } 
        }
    }

    private void reevaluateUnregisteredBuildings(ServerLevel level, PlayerInteractEvent.EntityInteract event)
    {
        VillageData villageData = getVillageData(level);
        for (VillageData.Building building : villageData.getUnregisteredBuildings())
        {
            ItemFrame frame = findFrame(level, building.position());
            if (frame == null
                    || !isBuildingValid(level, frame, building.type())
                    || !VillageData.isWithinVillage(level, building.position()))
            {
                continue;
            }

            if (villageData.registerBuilding(building.position(), building.type(), event, this))
            {
                villageData.removeUnregisteredBuilding(building);
            }
        }
    }

    private ItemFrame findFrame(ServerLevel level, BlockPos position)
    {
        return level.getEntitiesOfClass(ItemFrame.class, new AABB(position).inflate(1.0D),
                frame -> frame.blockPosition().equals(position)
                        && isStructureItem(frame.getItem()))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean isBuildingValid(ServerLevel level, ItemFrame frame, String type)
    {
        return switch (type)
        {
            case "town_hall" -> TownHallValidator.INSTANCE.isValid(level, frame);
            case "barracks" -> BarracksValidator.INSTANCE.isValid(level, frame);
            case "butcher" -> ButcherValidator.INSTANCE.isValid(level, frame);
            case "guard_post" -> GuardPostValidator.INSTANCE.isValid(level, frame);
            case "home" -> HomeValidator.INSTANCE.isValid(level, frame);
            case "kitchen" -> KitchenValidator.INSTANCE.isValid(level, frame);
            default -> GeneralValidator.INSTANCE.isValid(level, frame);
        };
    }

    public boolean isStructureItem(ItemStack stack)
    {
        return stack.is(TOWN_HALL_ITEM.get())
                || stack.is(BARRACKS_ITEM.get())
                || stack.is(BUTCHER_ITEM.get())
                || stack.is(GUARD_POST_ITEM.get())
                || stack.is(HOMES_ITEM.get())
                || stack.is(KITCHEN_ITEM.get())
                || stack.is(LIBRARY_ITEM.get())
                || stack.is(MERCHANT_STALL_ITEM.get())
                || stack.is(MINESHAFT_ITEM.get())
                || stack.is(RANCHER_PEN_ITEM.get())
                || stack.is(SCHOOL_ITEM.get())
                || stack.is(SMITHY_ITEM.get())
                || stack.is(STORAGE_ITEM.get())
                || stack.is(TAVERN_ITEM.get());
    }

    public String structureType(ItemStack stack)
    {
        return  stack.is(TOWN_HALL_ITEM.get()) ? "town_hall" :
                stack.is(BARRACKS_ITEM.get()) ? "barracks" : 
                stack.is(BUTCHER_ITEM.get()) ? "butcher" :
                stack.is(GUARD_POST_ITEM.get()) ? "gaurd_post" :
                stack.is(HOMES_ITEM.get()) ? "homes" :
                stack.is(KITCHEN_ITEM.get()) ? "kitchen" :
                stack.is(LIBRARY_ITEM.get()) ? "library" :
                stack.is(MERCHANT_STALL_ITEM.get()) ? "merchant_stall" :
                stack.is(MINESHAFT_ITEM.get()) ? "mineshaft" :
                stack.is(RANCHER_PEN_ITEM.get()) ? "rancher_pen" :
                stack.is(SCHOOL_ITEM.get()) ? "school" :
                stack.is(SMITHY_ITEM.get()) ? "smithy" :
                stack.is(STORAGE_ITEM.get()) ? "storage" :
                stack.is(TAVERN_ITEM.get()) ? "tavern" 
                /* defualt */               : "unknown";
    }

    private String structureName(ItemStack stack)
    {
        return  stack.is(TOWN_HALL_ITEM.get()) ? "Town Hall" :
                stack.is(BARRACKS_ITEM.get()) ? "Barracks" : 
                stack.is(BUTCHER_ITEM.get()) ? "Butcher House" :
                stack.is(GUARD_POST_ITEM.get()) ? "Gaurd Post" :        
                stack.is(HOMES_ITEM.get()) ? "Homes" :
                stack.is(KITCHEN_ITEM.get()) ? "Kitchen" :
                stack.is(LIBRARY_ITEM.get()) ? "Library" :
                stack.is(MERCHANT_STALL_ITEM.get()) ? "Merchant Stall" :
                stack.is(MINESHAFT_ITEM.get()) ? "Mineshaft" :
                stack.is(RANCHER_PEN_ITEM.get()) ? "Rancher Pen" :
                stack.is(SCHOOL_ITEM.get()) ? "School" :
                stack.is(SMITHY_ITEM.get()) ? "Smithy" :
                stack.is(STORAGE_ITEM.get()) ? "Storage" :
                stack.is(TAVERN_ITEM.get()) ? "Tavern" 
                /* default */               : "Unknown";
    }
}