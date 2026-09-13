package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public class BowEnhance extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> autoRelease = sg.add(new BoolSetting.Builder().name("auto-release").defaultValue(true).build());
    private final Setting<Integer> chargeTicks = sg.add(new IntSetting.Builder().name("charge-ticks").defaultValue(20).min(1).sliderMax(40).build());
    public BowEnhance() { super(Wxh1024Addon.WXH1024, "bow-enhance", "Improves bow charging and release handling."); }
    @EventHandler private void tick(TickEvent.Post event) {
        if (!autoRelease.get() || mc.player == null) return;
        ItemStack stack = mc.player.getActiveItem();
        if (stack.getItem() instanceof BowItem && mc.player.getItemUseTime() >= chargeTicks.get())
            mc.options.useKey.setPressed(false);
    }
}
