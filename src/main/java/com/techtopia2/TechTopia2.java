package com.techtopia2;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
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

    public static final DeferredBlock<Block> TECH_BLOCK = BLOCKS.registerSimpleBlock(
            "tech_block",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F));
    public static final DeferredItem<Item> TECH_ITEM = ITEMS.registerSimpleItem("tech_item");
    public static final DeferredItem<Item> BARRACKS_ITEM = ITEMS.registerSimpleItem("barracks");
    public static final DeferredItem<Item> BUTCHER_ITEM = ITEMS.registerSimpleItem("butcher");
    public static final DeferredItem<Item> GUARD_POST_ITEM = ITEMS.registerSimpleItem("guard_post");
        public static final DeferredHolder<EntityType<?>, EntityType<MaleNomadEntity>> MALE_NOMAD =
            ENTITIES.registerEntityType("male_nomad", MaleNomadEntity::new, MobCategory.CREATURE, builder ->
                builder.sized(0.6F, 1.8F));
        public static final DeferredHolder<EntityType<?>, EntityType<FemaleNomadEntity>> FEMALE_NOMAD =
            ENTITIES.registerEntityType("female_nomad", FemaleNomadEntity::new, MobCategory.CREATURE, builder ->
                builder.sized(0.6F, 1.8F));

    static
    {
        ITEMS.register("tech_block", key -> new BlockItem(
                TECH_BLOCK.get(),
            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key))));
    }

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

    private void createAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)
    {
        event.put(MALE_NOMAD.get(), NomadEntity.createAttributes().build());
        event.put(FEMALE_NOMAD.get(), NomadEntity.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(TECH_ITEM);
            event.accept(BARRACKS_ITEM);
            event.accept(BUTCHER_ITEM);
            event.accept(GUARD_POST_ITEM);
        }
    }

    @SubscribeEvent
    public void onItemFrameInteract(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getEntity().level().isClientSide()
                || !(event.getTarget() instanceof ItemFrame)
                || !isStructureItem(event.getItemStack()))
        {
            return;
        }

        Player player = event.getEntity();
        ItemFrame frame = (ItemFrame) event.getTarget();
        if (event.getItemStack().is(GUARD_POST_ITEM.get())
            && GuardPostValidator.INSTANCE.isValid(player.level(), frame))
        {
            registerBuilding(player, frame, "guard_post", "Guard Post");
        }
        else if (event.getItemStack().is(TECH_ITEM.get())
            && TownHallValidator.INSTANCE.isValid(player.level(), frame))
        {
            VillageData villageData = getVillageData((ServerLevel) player.level());
            if (villageData.registerTownHall(frame.blockPosition()))
            {
                player.sendSystemMessage(Component.literal(
                    "Town Hall recognized. A new village was registered with a "
                        + VillageData.VILLAGE_RADIUS + "-block radius."));
            }
            else
            {
                player.sendSystemMessage(Component.literal(
                        "Town Hall recognized. It is already inside a registered village."));
            }
        }
        else if (event.getItemStack().is(BARRACKS_ITEM.get())
            && BarracksValidator.INSTANCE.isValid(player.level(), frame))
        {
            registerBuilding(player, frame, "barracks", "Barracks");
        }
        else if (event.getItemStack().is(BUTCHER_ITEM.get())
            && ButcherValidator.INSTANCE.isValid(player.level(), frame))
        {
            registerBuilding(player, frame, "butcher", "Butcher");
        }
        else if (event.getItemStack().is(BARRACKS_ITEM.get()))
        {
                player.sendSystemMessage(Component.literal(
                    "Barracks needs 4 floor spaces, a 2-30 block height, a roof, and a door."));
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
    }

    private void registerBuilding(Player player, ItemFrame frame, String type, String displayName)
    {
        VillageData villageData = getVillageData((ServerLevel) player.level());
        if (villageData.registerBuilding(frame.blockPosition(), type))
        {
            player.sendSystemMessage(Component.literal(displayName + " structure registered to the village."));
        }
        else
        {
            player.sendSystemMessage(Component.literal(
                    displayName + " is valid, but it is outside the "
                        + VillageData.VILLAGE_RADIUS + "-block radius of a Town Hall."));
        }
    }

    private VillageData getVillageData(ServerLevel level)
    {
        return level.getServer().getDataStorage().computeIfAbsent(VillageData.TYPE);
    }

    private boolean isStructureItem(ItemStack stack)
    {
        return stack.is(TECH_ITEM.get())
                || stack.is(BARRACKS_ITEM.get())
                || stack.is(BUTCHER_ITEM.get())
                || stack.is(GUARD_POST_ITEM.get());
    }
}