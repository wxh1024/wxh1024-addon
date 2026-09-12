package com.wxh1024.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.wxh1024.addon.gui.SlimefunGuideScreen;
import com.wxh1024.addon.modules.SlimefunItemInspector;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/** Meteor replacement for the most visible SlimefunHelper client commands. */
public class SlimefunCommand extends Command {
    public SlimefunCommand() {
        super("slimefun", "Inspect items and open the Slimefun guide.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("id").executes(context -> {
            SlimefunItemInspector.copyMainHand();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("guide").executes(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) client.setScreen(new SlimefunGuideScreen(client.currentScreen));
            return SINGLE_SUCCESS;
        }));
    }
}
