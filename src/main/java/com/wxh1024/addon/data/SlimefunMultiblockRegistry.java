package com.wxh1024.addon.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.*;
import java.util.*;

/** Records and matches concrete 3x3 structures observed beside a recipe. */
public final class SlimefunMultiblockRegistry {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wxh1024-addon-multiblocks.json");
    private static final Map<String, List<String>> PATTERNS = new LinkedHashMap<>();
    private static boolean loaded;
    private SlimefunMultiblockRegistry() {}

    public static synchronized void load() {
        if (loaded) return; loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (var e : root.entrySet()) {
                List<String> p = new ArrayList<>();
                for (JsonElement v : e.getValue().getAsJsonArray()) p.add(v.getAsString());
                PATTERNS.put(e.getKey(), p);
            }
        } catch (Exception ignored) {}
    }

    public static synchronized boolean capture(String recipeId) {
        load();
        List<String> current = current();
        if (current.isEmpty() || recipeId == null || recipeId.isBlank()) return false;
        PATTERNS.put(recipeId, current); save(); return true;
    }

    public static synchronized String match() {
        load();
        List<String> current = current();
        return PATTERNS.entrySet().stream().filter(e -> e.getValue().equals(current)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public static synchronized int size() { load(); return PATTERNS.size(); }

    private static List<String> current() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return List.of();
        var origin = client.player.getBlockPos();
        List<String> result = new ArrayList<>(9);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var state = client.world.getBlockState(origin.add(x, -1, z));
            result.add(Registries.BLOCK.getId(state.getBlock()).toString());
        }
        return result;
    }

    private static void save() {
        try {
            JsonObject root = new JsonObject();
            PATTERNS.forEach((id, pattern) -> { JsonArray a = new JsonArray(); pattern.forEach(a::add); root.add(id, a); });
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {}
    }
}
