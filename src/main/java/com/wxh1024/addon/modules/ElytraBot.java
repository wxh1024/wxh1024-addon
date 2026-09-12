package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class ElytraBot extends Module { public ElytraBot(){super(Wxh1024Addon.WXH1024,"elytra-bot","Maintain level flight while gliding.");} @EventHandler private void tick(TickEvent.Post e){if(mc.player!=null&&mc.player.isGliding()){var v=mc.player.getVelocity();mc.player.setVelocity(v.x,Math.max(v.y,-.02),v.z);}}}
