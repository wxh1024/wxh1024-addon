package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
public class AutoResync extends Module {
    public AutoResync(){super(Wxh1024Addon.WXH1024,"auto-resync","Report server position corrections without spoofing movement packets.");}
    @EventHandler private void receive(PacketEvent.Receive e){if(e.packet instanceof PlayerPositionLookS2CPacket)info("服务器已同步玩家位置");}
}
