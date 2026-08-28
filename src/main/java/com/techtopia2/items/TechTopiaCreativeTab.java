package com.techtopia2.items;

import com.techtopia2.TechTopia2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB;

public class TechTopiaCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(CREATIVE_MODE_TAB, TechTopia2.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TECHTOPIA_TAB =
            CREATIVE_TABS.register("techtopia",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.techtopia2"))
                            .icon(() -> new ItemStack(TechTopiaItems.TOWN_HALL_ITEM.get()))
                            .displayItems((parameters, output) ->
                            {
                                output.accept(TechTopiaItems.TOWN_HALL_ITEM);
                                output.accept(TechTopiaItems.BARRACKS_ITEM);
                                output.accept(TechTopiaItems.BUTCHER_ITEM);
                                output.accept(TechTopiaItems.GUARD_POST_ITEM);
                                output.accept(TechTopiaItems.HOMES_ITEM);
                                output.accept(TechTopiaItems.KITCHEN_ITEM);
                                output.accept(TechTopiaItems.LIBRARY_ITEM);
                                output.accept(TechTopiaItems.MERCHANT_STALL_ITEM);
                                output.accept(TechTopiaItems.MINESHAFT_ITEM);
                                output.accept(TechTopiaItems.RANCHER_PEN_ITEM);
                                output.accept(TechTopiaItems.SCHOOL_ITEM);
                                output.accept(TechTopiaItems.SMITHY_ITEM);
                                output.accept(TechTopiaItems.STORAGE_ITEM);
                                output.accept(TechTopiaItems.TAVERN_ITEM);
                            })
                            .build());
}