package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Freecam extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(.5).min(.1).sliderMax(5).build()); private boolean old;
 public Freecam(){super(Wxh1024Addon.WXH1024,"freecam","Detach local collision and move the camera player.");}
 @Override public void onActivate(){if(mc.player!=null){old=mc.player.noClip;mc.player.noClip=true;}}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return;var v=mc.player.getRotationVector().multiply((mc.options.forwardKey.isPressed()?1:0)-(mc.options.backKey.isPressed()?1:0)).multiply(speed.get());mc.player.setVelocity(v);if(mc.options.jumpKey.isPressed())mc.player.addVelocity(0,speed.get(),0);if(mc.options.sneakKey.isPressed())mc.player.addVelocity(0,-speed.get(),0);}
 @Override public void onDeactivate(){if(mc.player!=null){mc.player.noClip=old;mc.player.setVelocity(0,0,0);}}
}
