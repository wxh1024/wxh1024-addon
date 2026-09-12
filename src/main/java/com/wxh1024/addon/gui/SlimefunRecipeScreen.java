package com.wxh1024.addon.gui;

import com.wxh1024.addon.data.SlimefunRecipeDatabase;
import com.wxh1024.addon.data.SavedItemStore;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Searchable choice/detail screen for recipes collected from Slimefun containers. */
public class SlimefunRecipeScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget search;
    private List<SlimefunRecipeDatabase.RecipeRecord> records = List.of();
    private int selected = -1;
    private int page;
    private static final int PAGE_SIZE = 8;

    public SlimefunRecipeScreen(Screen parent) {
        super(Text.literal("Slimefun Recipes"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        search = new TextFieldWidget(textRenderer, width / 2 - 180, 20, 260, 20, Text.literal("Search"));
        search.setChangedListener(value -> refresh(value));
        addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), button -> refresh(search.getText()))
                .dimensions(width / 2 + 90, 20, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(width / 2 - 45, height - 28, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save output"), button -> saveSelected())
                .dimensions(width / 2 + 90, 48, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
            if (page > 0) { page--; clearAndInit(); }
        }).dimensions(width / 2 - 180, height - 28, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
            if ((page + 1) * PAGE_SIZE < records.size()) { page++; clearAndInit(); }
        }).dimensions(width / 2 - 155, height - 28, 20, 20).build());
        refresh("");
        int start = page * PAGE_SIZE;
        for (int i = start; i < records.size() && i < start + PAGE_SIZE; i++) {
            final int index = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(records.get(i).output()),
                    button -> selected = index).dimensions(width / 2 - 180, 48 + (i - start) * 20, 220, 18).build());
        }

    }

    private void saveSelected() {
        if (selected < 0 || selected >= records.size()) return;
        var item = records.get(selected).outputStack();
        var identifier = net.minecraft.util.Identifier.tryParse(item.id());
        ItemStack stack = identifier == null ? ItemStack.EMPTY : new ItemStack(Registries.ITEM.get(identifier));
        if (!stack.isEmpty()) {
            stack.setCount(Math.max(1, item.count()));
            SavedItemStore.save(stack);
        }
    }

    private void refresh(String query) {
        records = SlimefunRecipeDatabase.query(query);
        page = 0;
        selected = records.isEmpty() ? -1 : Math.min(selected < 0 ? 0 : selected, records.size() - 1);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 180;
        context.drawText(textRenderer, title, left, 5, 0x55FFFF, true);
        int y = 52;
        int start = page * PAGE_SIZE;
        for (int i = start; i < records.size() && i < start + PAGE_SIZE; i++) {
            var record = records.get(i);
            int color = i == selected ? 0xFFFF55 : 0xDDDDDD;
            context.drawText(textRenderer, record.output() + " [" + record.machine().id() + "]", left, y, color, false);
            y += 20;
        }
        context.drawText(textRenderer, Text.literal("Page " + (page + 1) + "/" + Math.max(1, (records.size() + PAGE_SIZE - 1) / PAGE_SIZE)),
                left + 45, height - 22, 0xAAAAAA, false);
        if (selected >= 0) {
            var record = records.get(selected);
            int x = left + 245;
            context.drawText(textRenderer, Text.literal("Inputs"), x, 55, 0x55FF55, true);
            int line = 70;
            for (var input : record.inputs()) {
                if (input.count() > 0) {
                    context.drawText(textRenderer, Text.literal(input.id() + " x" + input.count()), x, line, 0xFFFFFF, false);
                    line += 13;
                }
            }
            context.drawText(textRenderer, Text.literal("Collected: " + record.collectedAt()), x, line + 8, 0xAAAAAA, false);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
