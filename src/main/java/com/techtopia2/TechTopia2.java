package com.techtopia2;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TechTopia2.MOD_ID)
public final class TechTopia2 {
    public static final String MOD_ID = "techtopia2";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public TechTopia2(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        LOGGER.info("TechTopia2 loaded");
    }
}