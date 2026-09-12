package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class SwimControl extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(1.2).min(0.1).sliderMax(3).build());
    public SwimControl(){super(Wxh1024Addon.WXH1024,"swim-control","Improve controlled movement in water.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||!mc.player.isSwimming())return; var v=mc.player.getVelocity(); mc.player.setVelocity(v.x*speed.get(),v.y,v.z*speed.get());}
}
