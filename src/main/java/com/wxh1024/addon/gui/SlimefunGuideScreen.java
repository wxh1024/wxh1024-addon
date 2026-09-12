package com.wxh1024.addon.gui;

import com.wxh1024.addon.utils.SlimefunItemInfo;
import com.wxh1024.addon.modules.SlimefunItemInspector;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import com.wxh1024.addon.data.SlimefunModelRegistry;
import com.wxh1024.addon.gui.SlimefunModelScreen;

/** Minimal runnable guide screen; recipe database integration is a later migration step. */
public class SlimefunGuideScreen extends Screen {
    private final Screen parent;

    public SlimefunGuideScreen(Screen parent) {
        super(Text.literal("Slimefun Guide"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 120;
        int y = height / 2 + 36;
        addDrawableChild(ButtonWidget.builder(Text.literal("复制当前物品 ID"),
                button -> SlimefunItemInspector.copyMainHand()).dimensions(x, y, 240, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("返回"),
                button -> close()).dimensions(x, y + 26, 240, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("浏览模型/材料"),
                button -> {
                    if (client != null) client.setScreen(new SlimefunModelScreen(this, ""));
                }).dimensions(x, y + 52, 240, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - 120;
        int y = height / 2 - 40;
        context.drawText(textRenderer, title, x, y, 0x55FFFF, true);
        context.drawText(textRenderer, Text.literal("当前物品: " + SlimefunItemInfo.id(SlimefunItemInfo.mainHand())),
                x, y + 24, 0xFFFFFF, false);
        context.drawText(textRenderer, Text.literal("使用 .slimefun id 复制物品 ID"),
                x, y + 44, 0xAAAAAA, false);
        context.drawText(textRenderer, Text.literal("按 ESC 返回"),
                x, y + 64, 0xAAAAAA, false);
        int line = y + 84;
        context.drawText(textRenderer, Text.literal("匹配数据: " + SlimefunModelRegistry.recipes(
                SlimefunItemInfo.id(SlimefunItemInfo.mainHand())).size()), x, line, 0x55FF55, false);
        for (var recipe : SlimefunModelRegistry.recipes(SlimefunItemInfo.id(SlimefunItemInfo.mainHand()))) {
            if (line > height - 25) break;
            line += 12;
            context.drawText(textRenderer, Text.literal(recipe.output() + " [" + recipe.type() + "]"),
                    x, line, 0xDDDDDD, false);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
