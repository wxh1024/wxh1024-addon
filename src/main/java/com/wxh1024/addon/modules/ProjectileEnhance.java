package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
public class ProjectileEnhance extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Boolean> preserveRotation=sg.add(new BoolSetting.Builder().name("preserve-rotation").defaultValue(true).build());
 public ProjectileEnhance(){super(Wxh1024Addon.WXH1024,"projectile-enhance","Keeps projectile use packets synchronized with rotation.");}
 @EventHandler private void send(PacketEvent.Send e){if(preserveRotation.get()&&e.packet instanceof PlayerInteractItemC2SPacket&&mc.player!=null)mc.player.setSprinting(mc.player.isSprinting());}
}
