package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/** Periodically reports safe server/player-list information, replacing the portable scanner portion. */
public class ServerInfoNotifier extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> interval = sg.add(new IntSetting.Builder().name("interval-seconds")
            .defaultValue(30).min(5).sliderMax(300).build());
    private int ticks;

    public ServerInfoNotifier() { super(Wxh1024Addon.WXH1024, "server-info-notifier", "Report server and player-list information."); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.getNetworkHandler() == null || ++ticks < interval.get() * 20) return;
        ticks = 0;
        int players = mc.getNetworkHandler().getPlayerList().size();
        String address = mc.getNetworkHandler().getConnection().getAddress().toString();
        info("服务器: " + address + "，在线玩家: " + players);
    }
}
