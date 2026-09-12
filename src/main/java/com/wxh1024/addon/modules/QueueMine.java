package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class QueueMine extends Module { public QueueMine(){super(Wxh1024Addon.WXH1024,"queue-mine","Continue normal block breaking while attack is held.");} @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.interactionManager==null||!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h)||!mc.options.attackKey.isPressed())return;mc.interactionManager.updateBlockBreakingProgress(h.getBlockPos(),h.getSide());}}
