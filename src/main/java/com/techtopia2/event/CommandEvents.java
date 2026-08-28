package com.techtopia2.event;

import com.techtopia2.command.TechTopiaCommands;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CommandEvents
{
    private CommandEvents()
    {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        TechTopiaCommands.register(event);
    }
}