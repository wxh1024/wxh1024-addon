package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
public class TransactionBlocker extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Boolean> blockPong=sg.add(new BoolSetting.Builder().name("block-pong").defaultValue(false).build());
 public TransactionBlocker(){super(Wxh1024Addon.WXH1024,"transaction-blocker","Optionally blocks transaction acknowledgements.");}
 @EventHandler private void send(PacketEvent.Send e){if(blockPong.get()&&e.packet instanceof CommonPongC2SPacket)e.cancel();}
}
