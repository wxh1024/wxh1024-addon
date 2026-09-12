package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
public class AutoWeb extends Module {
 public AutoWeb(){super(Wxh1024Addon.WXH1024,"auto-web","Place cobweb at the targeted block using normal interaction.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.interactionManager==null||!mc.player.getMainHandStack().isOf(Items.COBWEB)||!mc.options.useKey.isPressed())return;if(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult h)mc.interactionManager.interactBlock(mc.player,net.minecraft.util.Hand.MAIN_HAND,h);}
}
