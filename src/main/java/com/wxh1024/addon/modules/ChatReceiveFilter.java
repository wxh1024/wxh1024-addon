package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

public class ChatReceiveFilter extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<List<String>> blocked = sg.add(new StringListSetting.Builder().name("blocked-words")
            .defaultValue(List.of()).build());

    public ChatReceiveFilter() { super(Wxh1024Addon.WXH1024, "chat-receive-filter", "Hide configured noisy incoming chat messages."); }

    @EventHandler
    private void onMessage(ReceiveMessageEvent event) {
        String message = event.getMessage().getString().toLowerCase();
        if (blocked.get().stream().anyMatch(word -> !word.isBlank() && message.contains(word.toLowerCase()))) event.cancel();
    }
}
