package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class BoatVClip extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> amount=sg.add(new DoubleSetting.Builder().name("amount").defaultValue(3).min(.1).sliderMax(20).build()); private boolean last;
 public BoatVClip(){super(Wxh1024Addon.WXH1024,"boat-vclip","Move vertically while riding a vehicle.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||!mc.player.hasVehicle())return;boolean now=mc.options.jumpKey.isPressed();if(now&&!last)mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(),mc.player.getVehicle().getY()+amount.get(),mc.player.getVehicle().getZ());last=now;}
}
