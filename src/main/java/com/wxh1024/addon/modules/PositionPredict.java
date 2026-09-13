package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class PositionPredict extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> ticks = sg.add(new IntSetting.Builder().name("ticks").defaultValue(5).min(1).sliderMax(20).build());
    public PositionPredict() { super(Wxh1024Addon.WXH1024, "position-predict", "Renders predicted player positions."); }
    @EventHandler private void render(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) if (p != mc.player) {
            var v = p.getVelocity().multiply(ticks.get());
            event.renderer.box(p.getBoundingBox().offset(v), Color.YELLOW, Color.YELLOW, ShapeMode.Lines, 0);
        }
    }
}
