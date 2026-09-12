package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Criticals extends Module { public Criticals(){super(Wxh1024Addon.WXH1024,"criticals","Critical-hit helper using ordinary grounded jump timing.");} @EventHandler private void tick(TickEvent.Post e){if(mc.player!=null&&mc.options.attackKey.isPressed()&&mc.player.isOnGround()&&mc.options.jumpKey.isPressed())mc.player.jump();}}
