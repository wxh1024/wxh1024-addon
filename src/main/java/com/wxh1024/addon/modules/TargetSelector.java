package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
public class TargetSelector extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> range=sg.add(new DoubleSetting.Builder().name("range").defaultValue(12).min(1).sliderMax(64).build()); private Entity target;
 public TargetSelector(){super(Wxh1024Addon.WXH1024,"target-selector","Select the nearest living target.");}
 @EventHandler private void tick(TickEvent.Post e){target=null;if(mc.player==null||mc.world==null)return;double best=range.get()*range.get();for(Entity x:mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(range.get())))if(x.isAlive()&&mc.player.squaredDistanceTo(x)<best){best=mc.player.squaredDistanceTo(x);target=x;}}
 @Override public String getInfoString(){return target==null?"":target.getName().getString();}
}
