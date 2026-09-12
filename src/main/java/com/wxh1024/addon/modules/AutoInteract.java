package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
public class AutoInteract extends Module {
 public AutoInteract(){super(Wxh1024Addon.WXH1024,"auto-interact","Interact with the current block target while holding use.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.interactionManager==null||!(mc.crosshairTarget instanceof BlockHitResult hit)||!mc.options.useKey.isPressed())return;mc.interactionManager.interactBlock(mc.player,Hand.MAIN_HAND,hit);}
}
