package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
public class NoGround extends Module {
 public NoGround(){super(Wxh1024Addon.WXH1024,"no-ground","Send movement packets as airborne.");}
 @EventHandler private void send(PacketEvent.Send e){if(e.packet instanceof PlayerMoveC2SPacket p && p.isOnGround())e.cancel();}
}
