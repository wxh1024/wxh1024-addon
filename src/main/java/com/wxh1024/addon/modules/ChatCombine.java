package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class ChatCombine extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> window = sg.add(new IntSetting.Builder().name("window-ticks").defaultValue(40).min(1).sliderMax(200).build());
    private String last;
    private int repeats;
    private int age;

    public ChatCombine() { super(Wxh1024Addon.WXH1024, "chat-combine", "Collapse repeated incoming chat lines into one local message."); }

    @EventHandler
    private void onMessage(ReceiveMessageEvent event) {
        String text = event.getMessage().getString();
        if (text.equals(last) && age <= window.get()) {
            repeats++;
            event.cancel();
        } else {
            if (repeats > 0) info("上一条消息重复 " + (repeats + 1) + " 次");
            last = text;
            repeats = 0;
        }
        age = 0;
    }

    @EventHandler private void onTick(TickEvent.Post event) { age++; }
}
