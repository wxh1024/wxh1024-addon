package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CombatExtra extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> range = sg.add(new DoubleSetting.Builder().name("interaction-range").defaultValue(4.5).min(1).sliderMax(8).build());
    public CombatExtra() { super(Wxh1024Addon.WXH1024, "combat-extra", "Combat interaction helpers."); }
    public double range() { return range.get(); }
    @EventHandler private void tick(TickEvent.Post event) { }
}
