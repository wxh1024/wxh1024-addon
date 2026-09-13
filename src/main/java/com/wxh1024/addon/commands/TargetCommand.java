package com.wxh1024.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.command.CommandSource.suggestMatching;

public class TargetCommand extends Command {
    public TargetCommand() { super("target", "Send a target position to the server."); }
    @Override public void build(LiteralArgumentBuilder<CommandSource> b) {
        b.then(argument("x", DoubleArgumentType.doubleArg()).then(argument("y", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(c -> {
            if (mc.player != null) mc.player.networkHandler.sendChatCommand("tp " + getDouble(c,"x") + " " + getDouble(c,"y") + " " + getDouble(c,"z"));
            return SINGLE_SUCCESS;
        })))); 
    }
}
