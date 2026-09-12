package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class NoFall extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> threshold=sg.add(new DoubleSetting.Builder().name("threshold").defaultValue(3).min(0).sliderMax(20).build());
    public NoFall(){super(Wxh1024Addon.WXH1024,"no-fall","Reset local fall distance after a configurable threshold.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player!=null&&mc.player.fallDistance>threshold.get())mc.player.fallDistance=0;}
}
