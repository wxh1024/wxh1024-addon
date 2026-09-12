package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.List;

/** Safe Meteor-native chat utility: filters outgoing messages and normalizes punctuation. */
public class ChatFilter extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<List<String>> blocked = sg.add(new StringListSetting.Builder().name("blocked-words")
            .defaultValue(List.of()).build());
    private final Setting<Boolean> punctuation = sg.add(new BoolSetting.Builder().name("auto-punctuation")
            .defaultValue(false).build());

    public ChatFilter() {
        super(Wxh1024Addon.WXH1024, "chat-filter", "Filter and normalize outgoing chat messages.");
    }

    @EventHandler
    private void onSend(SendMessageEvent event) {
        String message = event.message;
        String lower = message.toLowerCase();
        if (blocked.get().stream().anyMatch(word -> !word.isBlank() && lower.contains(word.toLowerCase()))) {
            event.cancel();
            warning("消息被聊天过滤器拦截");
            return;
        }
        if (punctuation.get() && !message.startsWith("/") && !message.isBlank()
                && !message.matches(".*[.!?。！？]$")) event.message = message + "。";
    }
}
