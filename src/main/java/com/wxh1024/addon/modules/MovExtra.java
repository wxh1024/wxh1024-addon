package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class MovExtra extends Module {
 public MovExtra(){super(Wxh1024Addon.WXH1024,"mov-extra","Additional movement state helpers.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player!=null&&mc.player.isSneaking()&&mc.player.isOnGround())mc.player.setVelocity(mc.player.getVelocity().x,0,mc.player.getVelocity().z);}
}
