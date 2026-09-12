package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class GhostHand extends Module {
 public GhostHand(){super(Wxh1024Addon.WXH1024,"ghost-hand","Allow normal interaction with the targeted block.");}
 @EventHandler private void interact(InteractBlockEvent event){}
}
