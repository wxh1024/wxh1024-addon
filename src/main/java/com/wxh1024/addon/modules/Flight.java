package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Flight extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(0.5).min(0).sliderMax(5).build());
 public Flight(){super(Wxh1024Addon.WXH1024,"flight","Free flight movement.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return;mc.player.getAbilities().flying=true;mc.player.getAbilities().setFlySpeed(speed.get().floatValue()/10);mc.player.setVelocity(mc.player.getVelocity().x,0,mc.player.getVelocity().z);if(mc.options.jumpKey.isPressed())mc.player.setVelocity(mc.player.getVelocity().x,speed.get(),mc.player.getVelocity().z);if(mc.options.sneakKey.isPressed())mc.player.setVelocity(mc.player.getVelocity().x,-speed.get(),mc.player.getVelocity().z);}
 @Override public void onDeactivate(){if(mc.player!=null){mc.player.getAbilities().flying=false;mc.player.getAbilities().setFlySpeed(.05f);}}
}
