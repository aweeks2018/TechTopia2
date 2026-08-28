package com.techtopia2.event;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.techtopia2.data.village.VillageManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ServerTickEvents
{
    public static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels())
        {
            long dayTime = level.getGameTime() % 1000;

            if (dayTime == 0)
            {
                VillageManager villageManager = new VillageManager();
                villageManager.validateVillages(level);
            }
        }
    }
}
