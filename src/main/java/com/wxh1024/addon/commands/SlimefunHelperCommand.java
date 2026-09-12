package com.wxh1024.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.wxh1024.addon.gui.SlimefunGuideScreen;
import com.wxh1024.addon.gui.SlimefunModelScreen;
import com.wxh1024.addon.data.SlimefunModelRegistry;
import com.wxh1024.addon.utils.SlimefunItemInfo;
import com.wxh1024.addon.modules.SlimefunItemInspector;
import com.wxh1024.addon.modules.SlimefunTaskScheduler;
import com.wxh1024.addon.data.VanillaRecipeCache;
import com.wxh1024.addon.data.SavedItemStore;
import com.wxh1024.addon.gui.SlimefunRecipeScreen;
import com.wxh1024.addon.data.SlimefunRecipeDatabase;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.wxh1024.addon.data.SlimefunMultiblockRegistry;
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
            info("已采集配方: " + SlimefunRecipeDatabase.size() + " 条");
            openRecipes();
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("recipes").then(argument("query", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                .executes(c -> {
                    String query = c.getArgument("query", String.class);
                    var recipes = SlimefunModelRegistry.recipes(query);
                    var collected = SlimefunRecipeDatabase.query(query);
                    info("真实采集配方 " + collected.size() + " 条");
                    collected.stream().limit(10).forEach(r -> info(r.output() + " <- " + r.inputs().stream().map(i -> i.id() + "x" + i.count()).toList()));
                    if (recipes.isEmpty()) {
                        info("资源配方未找到；以上仅显示真实采集记录。");
                    }
                    info("匹配配方/材料 " + recipes.size() + " 条");
                    recipes.stream().limit(10).forEach(r -> info(r.output() + " <- " + String.join(", ", r.ingredients())));
                    return SINGLE_SUCCESS;
                })));
        builder.then(literal("recipes").then(literal("reload").executes(c -> {
            SlimefunRecipeDatabase.reload(); info("配方数据库已重新加载: " + SlimefunRecipeDatabase.size());
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("recipes").then(literal("clear").executes(c -> {
            SlimefunRecipeDatabase.clear(); info("已清除采集配方");
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("recipes").then(literal("gui").executes(c -> {
            openRecipes(); return SINGLE_SUCCESS;
        })));
        builder.then(literal("autofill").executes(c -> {
            if (!(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen)) {
                error("请先打开配方容器界面"); return SINGLE_SUCCESS;
            }
            var recipes = SlimefunRecipeDatabase.all();
            if (recipes.isEmpty()) { error("没有真实采集配方"); return SINGLE_SUCCESS; }
            info("已尝试填充: " + SlimefunRecipeDatabase.fillInputs(screen, recipes.get(0)) + " 个槽位");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("clearinputs").executes(c -> {
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> screen)
                info("已清空槽位: " + SlimefunRecipeDatabase.clearInputs(screen));
            else error("请先打开配方容器界面");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("multiblock").executes(c -> {
            String match = SlimefunMultiblockRegistry.match();
            info(match == null ? "未匹配已记录多方块结构 (已注册 " + SlimefunMultiblockRegistry.size() + " 个)"
                    : "匹配多方块配方: " + match);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("multiblock").then(literal("capture").then(argument("recipe",
                com.mojang.brigadier.arguments.StringArgumentType.greedyString()).executes(c -> {
                    String id = c.getArgument("recipe", String.class);
                    info(SlimefunMultiblockRegistry.capture(id) ? "已记录多方块结构: " + id : "无法记录结构，请进入世界");
                    return SINGLE_SUCCESS;
                }))));
        builder.then(literal("nbt").executes(c -> {
            info("当前物品 ID: " + SlimefunItemInfo.id(SlimefunItemInfo.mainHand()));
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("save").executes(c -> {
            String id = SlimefunItemInfo.id(SlimefunItemInfo.mainHand());
            SlimefunModelRegistry.recordItem(id);
            SavedItemStore.save(SlimefunItemInfo.mainHand());
            info("已保存观察物品: " + id);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("save").then(literal("clear").executes(c -> {
            int count = SlimefunModelRegistry.observedItems().size();
            SlimefunModelRegistry.observedItems().forEach(SlimefunModelRegistry::forgetObserved);
            info("已清除观察物品: " + count + " 条");
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("saved").executes(c -> {
            info("保存物品 " + SavedItemStore.ids().size() + " 条");
            SavedItemStore.ids().stream().limit(50).forEach(this::info);
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("saved").then(literal("clear").executes(c -> {
            SavedItemStore.clear(); info("已清空保存物品");
            return SINGLE_SUCCESS;
        })));
        builder.then(literal("vanilla").executes(c -> {
            info("原版配方缓存: " + VanillaRecipeCache.size() + " 条");
            return SINGLE_SUCCESS;
        }));
        builder.then(literal("vanilla").then(argument("query", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                .executes(c -> {
                    VanillaRecipeCache.query(c.getArgument("query", String.class)).stream().limit(10)
                            .forEach(r -> info(r.output().getName().getString() + " [" + r.station().getName().getString() + "]"));
                    return SINGLE_SUCCESS;
                })));
        builder.then(literal("vanilla").then(literal("clear").executes(c -> {
            VanillaRecipeCache.clear(); info("已清空原版配方缓存"); return SINGLE_SUCCESS;
        })));
    }

    private int openModels(String query) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.setScreen(new SlimefunModelScreen(client.currentScreen, query));
        return SINGLE_SUCCESS;
    }

    private void openRecipes() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.setScreen(new SlimefunRecipeScreen(client.currentScreen));
    }

    private int openGuide() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return SINGLE_SUCCESS;
        client.setScreen(new SlimefunGuideScreen(client.currentScreen));
        return SINGLE_SUCCESS;
    }
}
