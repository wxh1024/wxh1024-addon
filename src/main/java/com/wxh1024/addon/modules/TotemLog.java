package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class TotemLog extends Module {
    private int lastHealth;
    public TotemLog() { super(Wxh1024Addon.WXH1024, "totem-log", "Logs totem and lethal-health events."); }
    @Override public void onActivate() { lastHealth = mc.player == null ? 0 : (int) mc.player.getHealth(); }
    @EventHandler private void tick(TickEvent.Post event) {
        if (mc.player == null) return;
        int health = (int) mc.player.getHealth();
        if (lastHealth > 0 && health == 0) info("Player died or a totem was consumed.");
        lastHealth = health;
    }
}
