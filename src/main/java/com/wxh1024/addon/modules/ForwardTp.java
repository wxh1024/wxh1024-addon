package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class ForwardTp extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> distance=sg.add(new DoubleSetting.Builder().name("distance").defaultValue(3).min(.5).sliderMax(20).build()); private boolean last;
 public ForwardTp(){super(Wxh1024Addon.WXH1024,"forward-tp","Teleport forward when activated or triggered by jump.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return;boolean now=mc.options.jumpKey.isPressed();if(now&&!last){var p=mc.player.getRotationVector().multiply(distance.get());mc.player.requestTeleport(mc.player.getX()+p.x,mc.player.getY()+p.y,mc.player.getZ()+p.z);}last=now;}
}
