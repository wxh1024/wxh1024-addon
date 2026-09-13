package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class SpearEnhance extends Module {
    public SpearEnhance() { super(Wxh1024Addon.WXH1024, "spear-enhance", "Keeps spear use responsive."); }
    @EventHandler private void tick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (mc.player.isUsingItem() && mc.player.getActiveItem().isOf(Items.TRIDENT)) mc.player.setVelocity(mc.player.getVelocity().multiply(1.01, 1, 1.01));
    }
}
