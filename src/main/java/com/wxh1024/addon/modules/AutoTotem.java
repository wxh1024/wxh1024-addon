package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() { super(Wxh1024Addon.WXH1024, "auto-totem", "Keep a totem in the offhand when one is available."); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null || !mc.player.getOffHandStack().isEmpty()) return;
        int source = -1;
        for (int i = 0; i < mc.player.getInventory().size(); i++)
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) { source = i; break; }
        if (source < 0) return;
        int slot = source < 9 ? 36 + source : source;
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
    }
}
