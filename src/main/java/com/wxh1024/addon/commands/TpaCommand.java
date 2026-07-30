package com.wxh1024.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/** .tpa add/del/list —— 管理 tp 别名。 */
public class TpaCommand extends Command {
    public TpaCommand() {
        super("tpa", "管理 tp 别名指令", "tpaliases", "tpalias");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder
            .then(literal("add")
                .then(argument("name", StringArgumentType.word())
                    .then(argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            add(ctx.getArgument("name", String.class),
                                ctx.getArgument("message", String.class));
                            return SINGLE_SUCCESS;
                        }))))
            .then(literal("del")
                .then(argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        del(ctx.getArgument("name", String.class));
                        return SINGLE_SUCCESS;
                    })))
            .then(literal("list").executes(ctx -> {
                list();
                return SINGLE_SUCCESS;
            }))
            .executes(ctx -> {
                usage();
                return SINGLE_SUCCESS;
            });
    }

    private void add(String name, String msg) {
        if (name == null || name.isEmpty()) {
            error("用法: .tpa add <名称> <消息指令>");
            return;
        }
        if (msg == null || msg.isEmpty()) {
            error("消息指令不能为空: .tpa add <名称> <消息指令>");
            return;
        }
        boolean existed = TpAliasStore.add(name, msg);
        if (existed) info(CmdText.color("§a[TP] §7已更新别名 §b" + name + " §7为: §f" + msg));
        else info(CmdText.color("§a[TP] §7已添加别名 §b" + name + " §7: §f" + msg));
    }

    private void del(String name) {
        if (name == null || name.isEmpty()) {
            error("用法: .tpa del <名称>");
            return;
        }
        if (TpAliasStore.remove(name)) info(CmdText.color("§a[TP] §7已删除别名 §b" + name));
        else error("未找到别名 §b%s", name);
    }

    private void list() {
        Map<String, String> all = TpAliasStore.all();
        if (all.isEmpty()) {
            info(CmdText.color("§a[TP] §7暂无别名，使用 .tpa add <名称> <消息指令> 添加"));
            return;
        }
        info(CmdText.color("§a[TP别名] §7共 " + all.size() + " 条"));
        info(CmdText.color("§8========================"));
        for (Map.Entry<String, String> e : all.entrySet()) {
            info(CmdText.color("§8| §b" + e.getKey() + " §8=> §f" + e.getValue()));
        }
        info(CmdText.color("§8========================"));
    }

    private void usage() {
        info(CmdText.color("§a[TP] §7tp 别名管理"));
        info(CmdText.color("§8| §7.tpa add <名称> <消息指令>  §8- 添加/覆盖"));
        info(CmdText.color("§8| §7.tpa del <名称>            §8- 删除"));
        info(CmdText.color("§8| §7.tpa list                 §8- 查看全部"));
        info(CmdText.color("§8| §7.tp <名称>                §8- 触发"));
    }
}
