package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class VelocityControl extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> horizontal=sg.add(new DoubleSetting.Builder().name("horizontal").defaultValue(1).min(0).sliderMax(1).build());
    private final Setting<Double> vertical=sg.add(new DoubleSetting.Builder().name("vertical").defaultValue(1).min(0).sliderMax(1).build());
    public VelocityControl(){super(Wxh1024Addon.WXH1024,"velocity-control","Scale client velocity after knockback or explosions.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.player.hurtTime<=0)return;var v=mc.player.getVelocity();mc.player.setVelocity(v.x*horizontal.get(),v.y*vertical.get(),v.z*horizontal.get());}
}
