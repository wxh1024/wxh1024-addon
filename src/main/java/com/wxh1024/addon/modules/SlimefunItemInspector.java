package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import com.wxh1024.addon.utils.SlimefunItemInfo;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * First Meteor-native slice of SlimefunHelper's CopyId module.
 * Activate it to copy the main-hand item's vanilla identifier.
 */
public class SlimefunItemInspector extends Module {
    public SlimefunItemInspector() {
        super(Wxh1024Addon.WXH1024, "slimefun-item-inspector",
                "Inspect and copy the identifier of the item in your main hand.");
    }

    @Override
    public void onActivate() {
        copyMainHand();
        toggle();
    }

    public static void copyMainHand() {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemStack stack = SlimefunItemInfo.mainHand();
        if (stack.isEmpty()) {
            ChatUtils.sendMsg(Text.literal("手中没有物品"));
            return;
        }
        String id = SlimefunItemInfo.id(stack);
        client.keyboard.setClipboard(id);
        ChatUtils.sendMsg(Text.literal("已复制物品 ID: " + id));
    }
}
