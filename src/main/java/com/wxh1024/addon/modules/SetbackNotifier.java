package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
public class SetbackNotifier extends Module {
    public SetbackNotifier(){super(Wxh1024Addon.WXH1024,"setback-notifier","Notify when the server corrects client position.");}
    @EventHandler private void receive(PacketEvent.Receive e){if(e.packet instanceof PlayerPositionLookS2CPacket p)info("位置回滚: "+p.change());}
}
