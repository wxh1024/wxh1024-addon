package com.wxh1024.addon.gui;

import com.wxh1024.addon.data.SlimefunModelRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SlimefunModelScreen extends Screen {
    private final Screen parent;
    private final String query;

    public SlimefunModelScreen(Screen parent, String query) {
        super(Text.literal("Slimefun item models"));
        this.parent = parent;
        this.query = query == null ? "" : query;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("返回"), button -> close())
                .dimensions(width / 2 - 100, height - 35, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int x = width / 2 - 190;
        int y = 24;
        context.drawText(textRenderer, title, x, y, 0x55FFFF, true);
        context.drawText(textRenderer, Text.literal("映射总数: " + SlimefunModelRegistry.size()), x, y + 18, 0xFFFFFF, false);
        int line = y + 42;
        for (var entry : SlimefunModelRegistry.search(query)) {
            if (line > height - 50) break;
            context.drawText(textRenderer, Text.literal(entry.getKey() + " = " + entry.getValue()),
                    x, line, 0xDDDDDD, false);
            line += 12;
        }
        if (!query.isEmpty()) {
            context.drawText(textRenderer, Text.literal("查询: " + query), x + 220, y + 18, 0xFFFF55, false);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
