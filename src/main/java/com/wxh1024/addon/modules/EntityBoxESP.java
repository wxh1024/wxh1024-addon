package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityBoxESP extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> range = sg.add(new IntSetting.Builder().name("range").defaultValue(48).min(4).sliderMax(128).build());
    private final Setting<Boolean> playersOnly = sg.add(new BoolSetting.Builder().name("players-only").defaultValue(false).build());
    private final Color color = new Color(80, 220, 255, 180);

    public EntityBoxESP() { super(Wxh1024Addon.WXH1024, "entity-box-esp", "Draw simple boxes around nearby entities."); }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        for (Entity entity : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(range.get()))) {
            if (playersOnly.get() && !(entity instanceof PlayerEntity)) continue;
            event.renderer.box(entity.getBoundingBox(), color, color, ShapeMode.Lines, 0);
        }
    }
}
