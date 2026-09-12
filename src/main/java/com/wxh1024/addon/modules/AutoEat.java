package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEat extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> hunger = sg.add(new IntSetting.Builder().name("hunger").defaultValue(14).min(0).max(20).build());
    private int oldSlot = -1;

    public AutoEat() { super(Wxh1024Addon.WXH1024, "auto-eat", "Eat food from the hotbar when hunger is low."); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null || mc.player.getHungerManager().getFoodLevel() > hunger.get()) return;
        if (oldSlot < 0) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.contains(DataComponentTypes.FOOD)) { oldSlot = mc.player.getInventory().getSelectedSlot(); mc.player.getInventory().setSelectedSlot(i); break; }
            }
        }
        if (oldSlot >= 0) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            if (!mc.player.isUsingItem()) {
                mc.player.getInventory().setSelectedSlot(oldSlot);
                oldSlot = -1;
            }
        }
    }
}
