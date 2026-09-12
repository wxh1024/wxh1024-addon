package com.wxh1024.addon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import com.wxh1024.addon.data.SlimefunItemIds;

import java.util.Locale;

/** Small, dependency-free part of SlimefunHelper's item inspection feature. */
public final class SlimefunItemInfo {
    private SlimefunItemInfo() {}

    public static ItemStack mainHand() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == null ? ItemStack.EMPTY : client.player.getMainHandStack();
    }

    public static String id(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "空手";
        return SlimefunItemIds.find(stack).orElseGet(() -> Registries.ITEM.getId(stack.getItem()).toString());
    }

    public static String displayId(ItemStack stack) {
        return id(stack).toUpperCase(Locale.ROOT);
    }
}
