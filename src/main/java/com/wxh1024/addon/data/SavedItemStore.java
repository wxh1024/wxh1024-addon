package com.wxh1024.addon.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.nio.file.*;
import java.util.*;

/** Lightweight persistent saved-item store, replacing the old ItemStackData database. */
public final class SavedItemStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wxh1024-addon-saved-items.json");
    private static final Map<String, SlimefunRecipeDatabase.ItemRecord> ITEMS = new LinkedHashMap<>();
    private static boolean loaded;

    private SavedItemStore() {}

    private static void load() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (var e : root.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                ITEMS.put(e.getKey(), new SlimefunRecipeDatabase.ItemRecord(e.getKey(), o.get("count").getAsInt(),
                        o.has("customData") ? o.get("customData").getAsString() : ""));
            }
        } catch (Exception ignored) {}
    }

    public static void save(ItemStack stack) {
        load();
        if (stack == null || stack.isEmpty()) return;
        String id = SlimefunItemIds.find(stack).orElseGet(() -> Registries.ITEM.getId(stack.getItem()).toString());
        String custom = "";
        var component = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA);
        if (component != null) custom = component.copyNbt().toString();
        ITEMS.put(id, new SlimefunRecipeDatabase.ItemRecord(id, stack.getCount(), custom));
        persist();
    }

    public static List<String> ids() { load(); return ITEMS.keySet().stream().toList(); }
    public static void clear() { ITEMS.clear(); persist(); }
    public static String path() { return FILE.toString(); }

    private static void persist() {
        try {
            JsonObject root = new JsonObject();
            ITEMS.forEach((id, item) -> {
                JsonObject o = new JsonObject();
                o.addProperty("count", item.count()); o.addProperty("customData", item.customData());
                root.add(id, o);
            });
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {}
    }
}
