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
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
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

    public static final DeferredBlock<Block> TECH_BLOCK = BLOCKS.registerSimpleBlock(
            "tech_block",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F));
    public static final DeferredItem<Item> TECH_ITEM = ITEMS.registerSimpleItem("tech_item");
    public static final DeferredItem<Item> BARRACKS_ITEM = ITEMS.registerSimpleItem("barracks");
    public static final DeferredItem<Item> BUTCHER_ITEM = ITEMS.registerSimpleItem("butcher");
    public static final DeferredItem<Item> GUARD_POST_ITEM = ITEMS.registerSimpleItem("guard_post");

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
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("TechTopia2 loaded");
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
            player.sendSystemMessage(Component.literal("Guard Post recognized."));
        }
        else if (event.getItemStack().is(TECH_ITEM.get())
            && TownHallValidator.INSTANCE.isValid(player.level(), frame))
        {
            player.sendSystemMessage(Component.literal("Town Hall recognized."));
        }
        else if (event.getItemStack().is(BARRACKS_ITEM.get())
            && BarracksValidator.INSTANCE.isValid(player.level(), frame))
        {
            player.sendSystemMessage(Component.literal(
                    "Barracks structure recognized."));
        }
        else if (event.getItemStack().is(BUTCHER_ITEM.get())
            && ButcherValidator.INSTANCE.isValid(player.level(), frame))
        {
            player.sendSystemMessage(Component.literal(
                    "Butcher structure recognized."));
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

    private boolean isStructureItem(ItemStack stack)
    {
        return stack.is(TECH_ITEM.get())
                || stack.is(BARRACKS_ITEM.get())
                || stack.is(BUTCHER_ITEM.get())
                || stack.is(GUARD_POST_ITEM.get());
    }
}