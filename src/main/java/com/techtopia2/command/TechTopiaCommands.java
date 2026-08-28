package com.techtopia2.command;

import com.techtopia2.data.village.VillageManager;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class TechTopiaCommands
{
    private TechTopiaCommands()
    {
    }

    public static void register(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal("techtopia")
            .then( Commands.literal("delete_village")
                .executes(context ->
                {
                    ServerLevel level =
                        (ServerLevel) context.getSource().getLevel();

                    VillageManager villageManager =
                        new VillageManager();

                    if (villageManager.forceDeleteAllVillages(level))
                    {
                        context.getSource().sendSuccess(
                            () -> Component.literal("Village deleted."),
                            true
                        );
                        return 1;
                    }

                    context.getSource().sendFailure(
                        Component.literal(
                            "You are not within the bounds of a registered village."
                        )
                    );

                    return 0;
                })
            )
        );
    }
}