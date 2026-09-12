package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class MineBot extends Module { public MineBot(){super(Wxh1024Addon.WXH1024,"mine-bot","Mine the selected block target while enabled.");} @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.interactionManager==null||!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h))return;mc.interactionManager.updateBlockBreakingProgress(h.getBlockPos(),h.getSide());}}
