package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Freecam extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> speed=sg.add(new DoubleSetting.Builder().name("speed").defaultValue(1).min(0).sliderMax(10).build()); private boolean oldNoClip;
 public Freecam(){super(Wxh1024Addon.WXH1024,"freecam","Detach local collision and move independently.");}
 @Override public void onActivate(){if(mc.player!=null){oldNoClip=mc.player.noClip;mc.player.noClip=true;}}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return;double f=(mc.options.forwardKey.isPressed()?1.0:0.0)-(mc.options.backKey.isPressed()?1.0:0.0),s=(mc.options.rightKey.isPressed()?1.0:0.0)-(mc.options.leftKey.isPressed()?1.0:0.0);var look=mc.player.getRotationVector();var right=look.crossProduct(new net.minecraft.util.math.Vec3d(0,1,0)).normalize();var v=look.multiply(f*speed.get()).add(right.multiply(s*speed.get()));if(mc.options.jumpKey.isPressed())v=v.add(0.0,speed.get(),0.0);if(mc.options.sneakKey.isPressed())v=v.add(0.0,-speed.get(),0.0);mc.player.setVelocity(v);}
 @Override public void onDeactivate(){if(mc.player!=null){mc.player.noClip=oldNoClip;mc.player.setVelocity(0,0,0);}}
}
