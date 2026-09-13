package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Queues movement packets and releases them as one burst when disabled. */
public class Blink extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> autoFlush = sg.add(new BoolSetting.Builder().name("auto-flush").defaultValue(false).build());
    private final Setting<Integer> flushDelay = sg.add(new IntSetting.Builder().name("flush-delay").defaultValue(20).min(1).sliderMax(200).build());
    private final Setting<Boolean> cancelOnHurt = sg.add(new BoolSetting.Builder().name("flush-on-hurt").defaultValue(true).build());
    private final List<Packet<?>> queued = new ArrayList<>();
    private int ticks;
    public Blink() { super(Wxh1024Addon.WXH1024, "blink", "Delay movement packets until released."); }
    @Override public void onActivate() { queued.clear(); ticks = 0; }
    @Override public void onDeactivate() { flush(); }
    @EventHandler private void send(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket) { queued.add(event.packet); event.cancel(); }
    }
    @EventHandler private void tick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (cancelOnHurt.get() && mc.player.hurtTime > 0) flush();
        if (autoFlush.get() && ++ticks >= flushDelay.get()) flush();
    }
    private void flush() {
        if (mc.getNetworkHandler() != null) for (Packet<?> packet : List.copyOf(queued)) mc.getNetworkHandler().sendPacket(packet);
        queued.clear(); ticks = 0;
    }
}
