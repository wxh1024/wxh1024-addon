package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
public class CrystalAura extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Double> range=sg.add(new DoubleSetting.Builder().name("range").defaultValue(4).min(1).sliderMax(8).build());
 public CrystalAura(){super(Wxh1024Addon.WXH1024,"crystal-aura","Attack nearby end crystals using the normal client attack path.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null)return;for(EndCrystalEntity c:mc.world.getEntitiesByClass(EndCrystalEntity.class,mc.player.getBoundingBox().expand(range.get()),x->x.isAlive()))if(mc.player.squaredDistanceTo(c)<=range.get()*range.get())mc.interactionManager.attackEntity(mc.player,c);}
}
