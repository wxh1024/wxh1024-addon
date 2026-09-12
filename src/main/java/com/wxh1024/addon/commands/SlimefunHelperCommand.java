package com.wxh1024.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.wxh1024.addon.gui.SlimefunGuideScreen;
import com.wxh1024.addon.gui.SlimefunModelScreen;
import com.wxh1024.addon.data.SlimefunModelRegistry;
import com.wxh1024.addon.utils.SlimefunItemInfo;
import com.wxh1024.addon.modules.SlimefunItemInspector;
import com.wxh1024.addon.modules.SlimefunTaskScheduler;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/** Meteor command equivalent of the dependency-free !! SlimefunHelper commands. */
public class SlimefunHelperCommand extends Command {
    public SlimefunHelperCommand() {
        super("sfh", "SlimefunHelper tools (Meteor command replacement for !! commands).");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("id").executes(c -> {
            SlimefunItemInspector.copyMainHand();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("help").executes(c -> {
            info(".sfh help | list | config | task report | recipes <query> | nbt | save [id] | models [query]");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("list").executes(c -> {
            info("已观察物品 " + SlimefunModelRegistry.observedItems().size() + " 条");
            SlimefunModelRegistry.observedItems().stream().limit(20).forEach(this::info);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("config").executes(c -> {
            info("持久化文件: " + SlimefunModelRegistry.persistencePath());
            info("模型映射: " + SlimefunModelRegistry.size() + " 条");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("reload").executes(c -> {
            info("Meteor modules and commands are registered at addon startup; no legacy command bus is reloaded.");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("openmenu").executes(c -> openGuide()));
        builder.then(literal("openmenu").then(literal("guide").executes(c -> openGuide())));
        builder.then(literal("task").then(literal("report").executes(c -> {
            SlimefunTaskScheduler module = Modules.get().get(SlimefunTaskScheduler.class);
            if (module != null) module.report();
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("task").then(literal("list").executes(c -> {
            SlimefunTaskScheduler module = Modules.get().get(SlimefunTaskScheduler.class);
            info(module == null ? "任务模块未注册" : "任务记录数: " + module.historySize());
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("models").executes(c -> openModels("")));
        builder.then(literal("models").then(argument("query", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                .executes(c -> openModels(c.getArgument("query", String.class)))));
        builder.then(literal("model").then(argument("key", com.mojang.brigadier.arguments.StringArgumentType.word())
                .executes(c -> {
                    String key = c.getArgument("key", String.class);
                    var model = SlimefunModelRegistry.model(key);
                    if (model.isEmpty()) error("未找到模型映射: " + key);
                    else info(key + " = " + model.getAsInt());
                    return SINGLE_SUCCESS;
                })));
        builder.then(literal("recipes").executes(c -> {
            info("当前没有内置外部配方数据库；请提供 slimefun-recipes.json 资源后再查询。");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("recipes").then(argument("query", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                .executes(c -> {
                    String query = c.getArgument("query", String.class);
                    var recipes = SlimefunModelRegistry.recipes(query);
                    if (recipes.isEmpty()) {
                        info("未找到配方/材料。当前资源配方数量为 0 时不会伪造数据。");
                    }
                    info("匹配配方/材料 " + recipes.size() + " 条");
                    recipes.stream().limit(10).forEach(r -> info(r.output() + " <- " + String.join(", ", r.ingredients())));
                    return SINGLE_SUCCESS;
                })));
        builder.then(literal("nbt").executes(c -> {
            info("当前物品 ID: " + SlimefunItemInfo.id(SlimefunItemInfo.mainHand()));
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("save").executes(c -> {
            String id = SlimefunItemInfo.id(SlimefunItemInfo.mainHand());
            SlimefunModelRegistry.recordItem(id);
            info("已保存观察物品: " + id);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("save").then(literal("clear").executes(c -> {
            int count = SlimefunModelRegistry.observedItems().size();
            SlimefunModelRegistry.observedItems().forEach(SlimefunModelRegistry::forgetObserved);
            info("已清除观察物品: " + count + " 条");
            return SINGLE_SUCCESS;
        })));
    }

    private int openModels(String query) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.setScreen(new SlimefunModelScreen(client.currentScreen, query));
        return SINGLE_SUCCESS;
    }

    private int openGuide() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return SINGLE_SUCCESS;
        client.setScreen(new SlimefunGuideScreen(client.currentScreen));
        return SINGLE_SUCCESS;
    }
}
