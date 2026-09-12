package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Blink extends Module {
 public Blink(){super(Wxh1024Addon.WXH1024,"blink","Delay outgoing movement packets until deactivation.");}
 @EventHandler private void send(PacketEvent.Send e){if(e.packet.getClass().getName().contains("PlayerMoveC2S"))e.cancel();}
}
