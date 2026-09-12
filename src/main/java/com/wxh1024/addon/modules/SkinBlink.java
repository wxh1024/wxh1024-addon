package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class SkinBlink extends Module {
 public SkinBlink(){super(Wxh1024Addon.WXH1024,"skin-blink","Delay client animation packets.");}
 @EventHandler private void send(PacketEvent.Send e){if(e.packet.getClass().getName().contains("ClientCommandC2S"))e.cancel();}
}
