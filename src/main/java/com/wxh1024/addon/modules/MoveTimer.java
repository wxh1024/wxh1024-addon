package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class MoveTimer extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> multiplier=sg.add(new DoubleSetting.Builder().name("multiplier").defaultValue(1).min(.1).sliderMax(2).build());
    public MoveTimer(){super(Wxh1024Addon.WXH1024,"move-timer","Scale local movement velocity; keep at 1 for vanilla timing.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||multiplier.get()==1)return;var v=mc.player.getVelocity();mc.player.setVelocity(v.x*multiplier.get(),v.y,v.z*multiplier.get());}
}
