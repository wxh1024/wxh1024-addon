package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
public class Velocity extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Double> horizontal=sg.add(new DoubleSetting.Builder().name("horizontal").defaultValue(0).min(0).sliderMax(1).build());
 private final Setting<Double> vertical=sg.add(new DoubleSetting.Builder().name("vertical").defaultValue(0).min(0).sliderMax(1).build());
 public Velocity(){super(Wxh1024Addon.WXH1024,"velocity","Scale server knockback packets.");}
 @EventHandler private void receive(PacketEvent.Receive e){if(mc.player==null||!(e.packet instanceof EntityVelocityUpdateS2CPacket p)||p.getEntityId()!=mc.player.getId())return; e.cancel(); mc.player.setVelocity(mc.player.getVelocity().multiply(horizontal.get(),vertical.get(),horizontal.get()));}
}
