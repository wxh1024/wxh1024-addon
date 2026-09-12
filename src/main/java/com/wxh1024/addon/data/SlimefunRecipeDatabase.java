package com.wxh1024.addon.data;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;

import java.nio.file.*;
import java.util.*;

/** Client-side recipe collector for Slimefun guide/container screens. */
public final class SlimefunRecipeDatabase {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wxh1024-addon-recipes.json");
    private static final int[] INPUT_SLOTS = {3, 4, 5, 12, 13, 14, 21, 22, 23};
    private static final Map<String, RecipeRecord> RECIPES = new LinkedHashMap<>();
    private static boolean loaded;

    private SlimefunRecipeDatabase() {}

    public record ItemRecord(String id, int count, String customData) {}
    public record RecipeRecord(String output, ItemRecord outputStack, ItemRecord machine,
                               List<ItemRecord> inputs, long collectedAt) {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (var entry : root.entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                RECIPES.put(entry.getKey(), decode(value));
            }
        } catch (Exception ignored) {
            RECIPES.clear();
        }
    }

    public static synchronized int scan(HandledScreen<?> screen) {
        load();
        if (!isGuide(screen)) return 0;
        var slots = screen.getScreenHandler().slots;
        if (slots.size() < 27) return 0;
        ItemStack output = stack(slots, 16);
        if (output.isEmpty()) return 0;
        String outputId = SlimefunItemIds.find(output).orElseGet(() -> Registries.ITEM.getId(output.getItem()).toString());
        List<ItemRecord> inputs = new ArrayList<>();
        for (int slot : INPUT_SLOTS) inputs.add(item(slots, slot));
        RecipeRecord record = new RecipeRecord(outputId, item(output), item(slots, 10), inputs, System.currentTimeMillis());
        String key = signature(record);
        RecipeRecord previous = RECIPES.putIfAbsent(key, record);
        if (previous == null) {
            save();
            return 1;
        }
        return 0;
    }

    private static boolean isGuide(HandledScreen<?> screen) {
        String title = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        int slots = screen.getScreenHandler().slots.size();
        return title.contains("slimefun") || title.contains("指南") || title.contains("guide");
    }

    private static ItemStack stack(List<Slot> slots, int index) {
        return index >= 0 && index < slots.size() ? slots.get(index).getStack() : ItemStack.EMPTY;
    }

    private static ItemRecord item(List<Slot> slots, int index) {
        return item(stack(slots, index));
    }

    private static ItemRecord item(ItemStack stack) {
        if (stack.isEmpty()) return new ItemRecord("minecraft:air", 0, "");
        String id = SlimefunItemIds.find(stack).orElseGet(() -> Registries.ITEM.getId(stack.getItem()).toString());
        String data = "";
        NbtCompound custom = stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA) == null
                ? null : stack.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA).copyNbt();
        if (custom != null && !custom.isEmpty()) data = custom.toString();
        return new ItemRecord(id, stack.getCount(), data);
    }

    private static String signature(RecipeRecord record) {
        return record.output() + "|" + record.machine().id() + "|" + record.inputs().stream().map(ItemRecord::id).toList();
    }

    public static synchronized List<RecipeRecord> query(String query) {
        load();
        String needle = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return RECIPES.values().stream().filter(r -> needle.isEmpty()
                || r.output().toLowerCase(Locale.ROOT).contains(needle)
                || r.machine().id().toLowerCase(Locale.ROOT).contains(needle)
                || r.inputs().stream().anyMatch(i -> i.id().toLowerCase(Locale.ROOT).contains(needle))).limit(100).toList();
    }

    public static synchronized List<RecipeRecord> all() {
        load();
        return List.copyOf(RECIPES.values());
    }

    public static synchronized int clearInputs(net.minecraft.client.gui.screen.ingame.HandledScreen<?> screen) {
        if (!isGuide(screen) || screen.getScreenHandler().slots.size() < 27) return 0;
        int cleared = 0;
        for (int index : INPUT_SLOTS) {
            if (!stack(screen.getScreenHandler().slots, index).isEmpty()) {
                cleared++;
                clickSlot(screen, index);
            }
        }
        return cleared;
    }

    public static synchronized int fillInputs(net.minecraft.client.gui.screen.ingame.HandledScreen<?> screen,
                                              RecipeRecord recipe) {
        if (!isGuide(screen) || recipe == null || screen.getScreenHandler().slots.size() < 27) return 0;
        int filled = 0;
        for (int i = 0; i < INPUT_SLOTS.length && i < recipe.inputs().size(); i++) {
            ItemRecord wanted = recipe.inputs().get(i);
            if (wanted.count() <= 0 || !stack(screen.getScreenHandler().slots, INPUT_SLOTS[i]).isEmpty()) continue;
            for (int slot = 27; slot < screen.getScreenHandler().slots.size(); slot++) {
                ItemStack candidate = stack(screen.getScreenHandler().slots, slot);
                String id = candidate.isEmpty() ? "" : SlimefunItemIds.find(candidate)
                        .orElseGet(() -> Registries.ITEM.getId(candidate.getItem()).toString());
                if (id.equals(wanted.id())) {
                    clickSlot(screen, slot);
                    clickSlot(screen, INPUT_SLOTS[i]);
                    filled++;
                    break;
                }

            }
        }
        return filled;
    }

    /** Counts the non-air blocks in the player's immediate 3x3 footprint. */
    public static int nearbyStructureBlocks() {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return 0;
        var origin = client.player.getBlockPos();
        int count = 0;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (!client.world.getBlockState(origin.add(x, -1, z)).isAir()) count++;
        }
        return count;
    }

    private static void clickSlot(HandledScreen<?> screen, int slot) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.interactionManager != null && client.player != null) {
            client.interactionManager.clickSlot(screen.getScreenHandler().syncId, slot, 0,
                    net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
        }
    }

    public static synchronized int size() { load(); return RECIPES.size(); }

    public static synchronized void clear() { RECIPES.clear(); save(); }

    public static synchronized void reload() { loaded = false; RECIPES.clear(); load(); }

    private static void save() {
        try {
            JsonObject root = new JsonObject();
            for (var entry : RECIPES.entrySet()) root.add(entry.getKey(), encode(entry.getValue()));
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {}
    }

    private static JsonObject encode(RecipeRecord record) {
        JsonObject o = new JsonObject();
        o.addProperty("output", record.output());
        o.add("outputStack", encode(record.outputStack()));
        o.add("machine", encode(record.machine()));
        JsonArray inputs = new JsonArray();
        record.inputs().forEach(i -> inputs.add(encode(i)));
        o.add("inputs", inputs);
        o.addProperty("collectedAt", record.collectedAt());
        return o;
    }

    private static JsonObject encode(ItemRecord item) {
        JsonObject o = new JsonObject();
        o.addProperty("id", item.id()); o.addProperty("count", item.count()); o.addProperty("customData", item.customData());
        return o;
    }

    private static RecipeRecord decode(JsonObject o) {
        List<ItemRecord> inputs = new ArrayList<>();
        if (o.has("inputs")) for (JsonElement e : o.getAsJsonArray("inputs")) inputs.add(decodeItem(e.getAsJsonObject()));
        return new RecipeRecord(o.get("output").getAsString(), decodeItem(o.getAsJsonObject("outputStack")),
                decodeItem(o.getAsJsonObject("machine")), inputs, o.get("collectedAt").getAsLong());
    }

    private static ItemRecord decodeItem(JsonObject o) {
        return new ItemRecord(o.get("id").getAsString(), o.get("count").getAsInt(),
                o.has("customData") ? o.get("customData").getAsString() : "");
    }
}
