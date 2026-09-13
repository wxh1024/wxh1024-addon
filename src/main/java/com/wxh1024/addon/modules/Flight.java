package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Flight extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(.8).min(0).sliderMax(10).build()); private boolean oldAllow,oldFlying;
 public Flight(){super(Wxh1024Addon.WXH1024,"flight","Motion flight with vertical controls.");}
 @Override public void onActivate(){if(mc.player!=null){oldAllow=mc.player.getAbilities().allowFlying;oldFlying=mc.player.getAbilities().flying;}}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return;var a=mc.player.getAbilities();a.allowFlying=true;a.flying=true;var v=mc.player.getVelocity();double y=(mc.options.jumpKey.isPressed()?speed.get():0)-(mc.options.sneakKey.isPressed()?speed.get():0);mc.player.setVelocity(v.x,y,v.z);if(mc.options.forwardKey.isPressed())mc.player.addVelocity(mc.player.getRotationVector().multiply(speed.get()));}
 @Override public void onDeactivate(){if(mc.player!=null){mc.player.getAbilities().allowFlying=oldAllow;mc.player.getAbilities().flying=oldFlying;mc.player.setVelocity(0,0,0);}}
}
