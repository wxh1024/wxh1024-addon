package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class PearlFly extends Module {
 public PearlFly(){super(Wxh1024Addon.WXH1024,"pearl-fly","Maintain controlled movement after pearl use.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||!mc.player.isUsingItem())return;var v=mc.player.getVelocity();mc.player.setVelocity(v.x,v.y+0.02,v.z);}
}
