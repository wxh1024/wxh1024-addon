package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;

public class AttackAura extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> range = sg.add(new DoubleSetting.Builder().name("range").defaultValue(4).min(1).sliderMax(8).build());
    private final Setting<Boolean> playersOnly = sg.add(new BoolSetting.Builder().name("players-only").defaultValue(false).build());
    private final Setting<Boolean> respectCooldown = sg.add(new BoolSetting.Builder().name("respect-cooldown").defaultValue(true).build());
    public AttackAura() { super(Wxh1024Addon.WXH1024, "attack-aura", "Continuously attacks the nearest entity."); }
    @EventHandler private void tick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (respectCooldown.get() && mc.player.getAttackCooldownProgress(0.0f) < 1.0f) return;
        Entity target = null; double best = range.get() * range.get();
        for (Entity e : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(range.get()))) {
            if (!e.isAlive() || (playersOnly.get() && !(e instanceof net.minecraft.entity.player.PlayerEntity))
                || !mc.player.canSee(e)) continue;
            double d = mc.player.squaredDistanceTo(e);
            if (d < best) { best = d; target = e; }
        }
        if (target != null) mc.interactionManager.attackEntity(mc.player, target);
    }
}
