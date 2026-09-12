package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
public class Attack extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> range=sg.add(new DoubleSetting.Builder().name("range").defaultValue(3).min(1).sliderMax(6).build());
 public Attack(){super(Wxh1024Addon.WXH1024,"attack","Attack the nearest visible entity in range.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null)return;Entity target=null;double best=range.get()*range.get();for(Entity x:mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(range.get()))){double d=mc.player.squaredDistanceTo(x);if(x.isAlive()&&d<best){best=d;target=x;}}if(target!=null&&mc.options.attackKey.isPressed())mc.interactionManager.attackEntity(mc.player,target);}
}
