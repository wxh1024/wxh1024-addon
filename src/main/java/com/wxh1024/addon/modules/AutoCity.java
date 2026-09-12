package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class AutoCity extends Module {
 public AutoCity(){super(Wxh1024Addon.WXH1024,"auto-city","Break the currently targeted block while attack is held.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.interactionManager==null||!mc.options.attackKey.isPressed()||!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h))return;mc.interactionManager.updateBlockBreakingProgress(h.getBlockPos(),h.getSide());}
}
