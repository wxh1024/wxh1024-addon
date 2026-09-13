package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
public class SetBackLog extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Boolean> logPosition=sg.add(new BoolSetting.Builder().name("log-position").defaultValue(true).build());
 public SetBackLog(){super(Wxh1024Addon.WXH1024,"set-back-log","Logs server position correction packets.");}
 @EventHandler private void receive(PacketEvent.Receive e){if(logPosition.get()&&e.packet instanceof PlayerPositionLookS2CPacket p)info("Position resync: "+p.change());}
}
