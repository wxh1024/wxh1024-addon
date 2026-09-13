package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;

public class CombatManager extends Module {
    public static CombatManager INSTANCE;
    private Entity target;
    public CombatManager() { super(Wxh1024Addon.WXH1024, "combat-manager", "Tracks the nearest combat target."); INSTANCE = this; }
    public Entity getTarget() { return target; }
    @EventHandler private void tick(TickEvent.Post event) {
        target = null;
        if (mc.player == null || mc.world == null) return;
        double best = 16;
        for (Entity e : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(4))) {
            double d = mc.player.squaredDistanceTo(e);
            if (e.isAlive() && d < best) { best = d; target = e; }
        }
    }
}
