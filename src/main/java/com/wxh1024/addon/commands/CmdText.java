package com.wxh1024.addon.commands;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

/** 把含 § 颜色码的字符串转换为带格式的 Text 组件（每段仅应用最近一次颜色/格式）。 */
public final class CmdText {
    private static final Map<Character, Formatting> CODE_MAP = new HashMap<>();
    static {
        for (Formatting f : Formatting.values()) {
            Character code = f.getCode();
            if (code != null) CODE_MAP.put(Character.toLowerCase(code), f);
        }
    }

    private CmdText() {}

    public static Text color(String s) {
        MutableText root = Text.literal("");
        StringBuilder buf = new StringBuilder();
        Formatting cur = Formatting.WHITE;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                if (buf.length() > 0) {
                    root.append(Text.literal(buf.toString()).formatted(cur));
                    buf.setLength(0);
                }
                Formatting f = CODE_MAP.get(Character.toLowerCase(s.charAt(++i)));
                if (f != null) cur = f;
            } else {
                buf.append(c);
            }
        }
        if (buf.length() > 0) root.append(Text.literal(buf.toString()).formatted(cur));
        return root;
    }
}
