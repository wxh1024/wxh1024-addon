package com.wxh1024.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/** .tp <名称> —— 触发预设的别名指令（发送对应消息）。 */
public class TpCommand extends Command {
    public TpCommand() {
        super("tp", "触发预设的别名指令: .tp <名称>  (别名用 .tpa 管理)");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("name", StringArgumentType.string())
            .executes(ctx -> {
                execute(ctx.getArgument("name", String.class));
                return SINGLE_SUCCESS;
            })
        );
    }

    private void execute(String name) {
        if (name == null || name.isEmpty()) {
            error("用法: .tp <名称>");
            return;
        }
        String msg = TpAliasStore.get(name);
        if (msg == null) {
            error("未找到别名 §b%s §7对应的指令，请先用 .tpa add 添加", name);
            return;
        }
        info(CmdText.color("§a[TP] §7触发别名 §b" + name + " §7: §f" + msg));
        ChatUtils.sendPlayerMsg(msg);
    }
}
