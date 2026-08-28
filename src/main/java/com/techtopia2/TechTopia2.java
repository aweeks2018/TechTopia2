package com.techtopia2;
import com.techtopia2.entity.TechTopiaEntities;
import com.techtopia2.event.BuildingEvents;
import com.techtopia2.event.CommandEvents;
import com.techtopia2.event.ServerTickEvents;
import com.techtopia2.items.TechTopiaCreativeTab;
import com.techtopia2.items.TechTopiaItems;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(TechTopia2.MOD_ID)
public final class TechTopia2
{
    public static final String MOD_ID = "techtopia2";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public TechTopia2(IEventBus modEventBus)
    {
        TechTopiaItems.ITEMS.register(modEventBus);
        TechTopiaEntities.ENTITIES.register(modEventBus);

        TechTopiaCreativeTab.CREATIVE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(CommandEvents.class);
        NeoForge.EVENT_BUS.register(BuildingEvents.class);
        NeoForge.EVENT_BUS.register(ServerTickEvents.class);

        LOGGER.info("TechTopia2 loaded");
    }
}