package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class TravellingControl extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(1).min(.1).sliderMax(3).build());
 public TravellingControl(){super(Wxh1024Addon.WXH1024,"travelling-control","Control travel speed while sprinting.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||!mc.player.isSprinting())return;var v=mc.player.getVelocity();mc.player.setVelocity(v.x*speed.get(),v.y,v.z*speed.get());}
}
