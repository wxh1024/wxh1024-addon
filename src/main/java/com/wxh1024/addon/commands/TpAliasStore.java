package com.wxh1024.addon.commands;

import meteordevelopment.meteorclient.MeteorClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** tp 别名持久化：名称 -> 消息指令，保存在 Meteor 配置目录的 tp_aliases.txt。 */
public final class TpAliasStore {
    private static final File FILE = new File(MeteorClient.FOLDER, "tp_aliases.txt");
    private static final char SEP = '\u0001'; // 名称与消息的分隔符（聊天消息中不会出现）
    private static final Map<String, String> MAP = new LinkedHashMap<>();
    private static boolean loaded = false;

    private TpAliasStore() {}

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!FILE.exists()) return;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty()) continue;
                int i = line.indexOf(SEP);
                if (i <= 0) continue;
                MAP.put(line.substring(0, i), line.substring(i + 1));
            }
        } catch (IOException ignored) {
            // 读取失败时当作空配置
        }
    }

    private static void save() {
        try {
            File parent = FILE.getParentFile();
            if (parent != null) parent.mkdirs();
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8))) {
                for (Map.Entry<String, String> e : MAP.entrySet()) {
                    w.write(e.getKey());
                    w.write(SEP);
                    w.write(e.getValue());
                    w.newLine();
                }
            }
        } catch (IOException ignored) {
            // 保存失败不影响游戏运行
        }
    }

    /** 新增/覆盖别名。返回是否存在同名的旧条目。 */
    public static boolean add(String name, String msg) {
        ensureLoaded();
        boolean existed = MAP.containsKey(name);
        MAP.put(name, msg);
        save();
        return existed;
    }

    /** 删除别名。返回是否真的删除了内容。 */
    public static boolean remove(String name) {
        ensureLoaded();
        boolean removed = MAP.remove(name) != null;
        if (removed) save();
        return removed;
    }

    public static String get(String name) {
        ensureLoaded();
        return MAP.get(name);
    }

    public static boolean contains(String name) {
        ensureLoaded();
        return MAP.containsKey(name);
    }

    public static Map<String, String> all() {
        ensureLoaded();
        return new LinkedHashMap<>(MAP);
    }
}
