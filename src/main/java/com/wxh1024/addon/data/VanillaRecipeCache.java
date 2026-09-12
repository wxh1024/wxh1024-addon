package com.wxh1024.addon.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.util.Identifier;

import java.util.*;

/** Meteor-side cache of the vanilla recipe-book entries available to the player. */
public final class VanillaRecipeCache {
    private static final Map<String, Entry> CACHE = new LinkedHashMap<>();
    private static long lastScan;

    public record Entry(String id, ItemStack station, ItemStack output, long scannedAt) {}

    private VanillaRecipeCache() {}

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.player.getRecipeBook() == null) return;
        if (client.world.getTime() == lastScan) return;
        lastScan = client.world.getTime();
        try {
            var context = SlotDisplayContexts.createParameters(client.world);
            var field = client.player.getRecipeBook().getClass().getDeclaredField("recipes");
            field.setAccessible(true);
            Map<?, ?> recipes = (Map<?, ?>) field.get(client.player.getRecipeBook());
            for (var entry : recipes.entrySet()) {
                RecipeDisplayEntry display = (RecipeDisplayEntry) entry.getValue();
                var stacks = display.getStacks(context);
                if (stacks.isEmpty()) continue;
                ItemStack output = stacks.get(0);
                ItemStack station = display.display().craftingStation().getFirst(context);
                String id = String.valueOf(entry.getKey());
                try {
                    id = String.valueOf(entry.getKey().getClass().getMethod("index").invoke(entry.getKey()));
                } catch (ReflectiveOperationException ignored) {}
                CACHE.put(id, new Entry(id, station, output, lastScan));
            }
        } catch (Throwable ignored) {
            // Recipe display APIs vary between Minecraft versions; cache remains usable.
        }
    }

    public static List<Entry> query(String query) {
        tick();
        String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return CACHE.values().stream().filter(e -> needle.isEmpty()
                || e.id().toLowerCase(Locale.ROOT).contains(needle)
                || e.output().getName().getString().toLowerCase(Locale.ROOT).contains(needle)
                || e.station().getName().getString().toLowerCase(Locale.ROOT).contains(needle)).limit(100).toList();
    }

    public static int size() { tick(); return CACHE.size(); }
    public static void clear() { CACHE.clear(); }
}
