package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

/** Portable survival safety utility based only on Meteor TickEvent. */
public class TotemSafetyNotifier extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> health = sg.add(new DoubleSetting.Builder().name("health-threshold")
            .defaultValue(8.0).min(0).sliderMax(20).build());
    private boolean warned;

    public TotemSafetyNotifier() {
        super(Wxh1024Addon.WXH1024, "totem-safety-notifier",
                "Warns when health is low and no totem is available.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        boolean hasTotem = mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)
                || mc.player.getInventory().contains(stack -> stack.isOf(Items.TOTEM_OF_UNDYING));
        if (mc.player.getHealth() <= health.get() && !hasTotem && !warned) {
            warning("生命值较低且背包没有不死图腾");
            warned = true;
        } else if (mc.player.getHealth() > health.get() + 2) warned = false;
    }
}
