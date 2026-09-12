package com.wxh1024.addon.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.*;
import java.nio.file.*;
import net.fabricmc.loader.api.FabricLoader;

/** Lightweight parser for the Slimefun model mapping shipped by the addon. */
public final class SlimefunModelRegistry {
    private static final Pattern ENTRY = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*:\\s*(\\d+)\\s*$");
    private static final Map<String, Integer> MODELS = load();
    private static final Map<String, Recipe> RECIPES = loadRecipes();
    private static final Set<String> OBSERVED = loadObserved();

    private SlimefunModelRegistry() {}

    private static Map<String, Integer> load() {
        Map<String, Integer> result = new TreeMap<>();
        var stream = SlimefunModelRegistry.class.getClassLoader()
                .getResourceAsStream("slimefun-item-model.yml");
        if (stream == null) return result;
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = ENTRY.matcher(line);
                if (matcher.matches()) result.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            }
        } catch (Exception ignored) {
            result.clear();
        }
        return Collections.unmodifiableMap(result);
    }

    public static OptionalInt model(String key) {
        Integer value = MODELS.get(normalize(key));
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public static List<Map.Entry<String, Integer>> search(String query) {
        String needle = normalize(query);
        return MODELS.entrySet().stream()
                .filter(entry -> needle.isEmpty() || entry.getKey().contains(needle))
                .limit(100)
                .toList();
    }

    public static int size() {
        return MODELS.size();
    }

    public record Recipe(String output, List<String> ingredients, String type) {}

    private static Map<String, Recipe> loadRecipes() {
        Map<String, Recipe> result = new TreeMap<>();
        var stream = SlimefunModelRegistry.class.getClassLoader().getResourceAsStream("slimefun-recipes.json");
        if (stream != null) {
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                root.entrySet().forEach(e -> {
                    JsonObject r = e.getValue().getAsJsonObject();
                    List<String> ingredients = new ArrayList<>();
                    if (r.has("ingredients")) for (JsonElement ingredient : r.getAsJsonArray("ingredients")) {
                        ingredients.add(ingredient.getAsString());
                    }
                    result.put(e.getKey(), new Recipe(e.getKey(), ingredients,
                            r.has("type") ? r.get("type").getAsString() : "unknown"));
                });
            } catch (Exception ignored) {}
        }
        return result;
    }

    public static List<Recipe> recipes(String query) {
        String needle = normalize(query);
        return RECIPES.values().stream().filter(r -> r.output().contains(needle)
                || r.ingredients().stream().anyMatch(i -> i.toUpperCase(Locale.ROOT).contains(needle))).limit(100).toList();
    }

    public static Optional<Recipe> recipe(String output) {
        return Optional.ofNullable(RECIPES.get(normalize(output)));
    }

    public static void recordItem(String id) {
        if (id == null || id.isBlank()) return;
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("wxh1024-addon-items.json");
            JsonObject root = Files.exists(path) ? JsonParser.parseString(Files.readString(path)).getAsJsonObject() : new JsonObject();
            root.addProperty(id, System.currentTimeMillis());
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(root));
            OBSERVED.add(id);
        } catch (Exception ignored) {}
    }

    private static Set<String> loadObserved() {
        Set<String> result = new TreeSet<>();
        try {
            Path path = dataPath();
            if (Files.exists(path)) {
                JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                result.addAll(root.keySet());
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static Path dataPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("wxh1024-addon-items.json");
    }

    public static List<String> observedItems() {
        return OBSERVED.stream().toList();
    }

    public static boolean forgetObserved(String id) {
        if (!OBSERVED.remove(id)) return false;
        try {
            Path path = dataPath();
            JsonObject root = Files.exists(path)
                    ? JsonParser.parseString(Files.readString(path)).getAsJsonObject() : new JsonObject();
            root.remove(id);
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {}
        return true;
    }

    public static String persistencePath() {
        return dataPath().toString();
    }

    public static String keyForModel(int model) {
        return MODELS.entrySet().stream()
                .filter(entry -> entry.getValue() == model)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
