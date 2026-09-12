package com.wxh1024.addon.data;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Optional;

/** Extracts the common Slimefun/PDC-style identifiers without a Bukkit dependency. */
public final class SlimefunItemIds {
    private static final List<String> KEYS = List.of(
            "id", "ID", "sfid", "sf-id", "slimefun_id", "slimefun:id", "slimefun-id", "item-id");

    private SlimefunItemIds() {}

    public static Optional<String> find(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null) return Optional.empty();
        return find(custom.copyNbt());
    }

    public static Optional<String> find(NbtCompound nbt) {
        for (String key : KEYS) {
            Optional<String> value = text(nbt, key);
            if (value.isPresent() && !value.get().isBlank()) return value;
        }
        for (String parent : List.of("PublicBukkitValues", "publicbukkitvalues", "slimefun", "Slimefun")) {
            NbtElement element = nbt.get(parent);
            if (element instanceof NbtCompound child) {
                Optional<String> nested = find(child);
                if (nested.isPresent()) return nested;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> text(NbtCompound nbt, String key) {
        NbtElement element = nbt.get(key);
        return element == null ? Optional.empty() : element.asString();
    }
}
