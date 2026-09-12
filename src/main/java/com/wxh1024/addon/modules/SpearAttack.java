package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class SpearAttack extends Module {
 public SpearAttack(){super(Wxh1024Addon.WXH1024,"spear-attack","Use the normal attack input while holding a spear.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player!=null&&mc.options.attackKey.isPressed()&&mc.player.getMainHandStack().getName().getString().toLowerCase().contains("spear"))mc.options.attackKey.setPressed(true);}
}
