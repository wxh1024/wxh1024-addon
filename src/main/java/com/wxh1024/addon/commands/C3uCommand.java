package com.wxh1024.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class C3uCommand extends Command {
    private static final String BASE = "https://3c3u.info";
    private static final ZoneId TZ = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public C3uCommand() {
        super("3c3u", "查询 3c3u.info 服务器上指定玩家的信息。用法: .3c3u <玩家名>", "c3u");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("name", StringArgumentType.string())
            .executes(ctx -> {
                query(ctx.getArgument("name", String.class));
                return SINGLE_SUCCESS;
            })
        );
    }

    private void query(String name) {
        if (name == null || name.isEmpty()) {
            error("请输入玩家名: .3c3u <玩家名>");
            return;
        }
        info(color("§7正在查询玩家 §b" + name + " §7的信息..."));
        new Thread(() -> {
            try {
                String json = fetch(name);
                PlayerData d = PlayerData.parse(json);
                MinecraftClient.getInstance().execute(() -> printResult(d));
            } catch (java.io.IOException | InterruptedException e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String finalMsg = msg;
                MinecraftClient.getInstance().execute(() -> error("网络请求失败 (%s)", finalMsg));
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                String finalMsg = msg;
                MinecraftClient.getInstance().execute(() -> error("%s", finalMsg));
            }
        }).start();
    }

    private static String fetch(String name) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = BASE + "/api/getPlayer?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "wxh1024-addon/1.0")
            .timeout(java.time.Duration.ofSeconds(15))
            .GET()
            .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new java.io.IOException("HTTP " + resp.statusCode());
        return resp.body();
    }

    private void printResult(PlayerData d) {
        boolean online = d.lastLogin > d.lastLogout;
        info(color("§b" + d.name + " §a[§" + (online ? "a在线" : "c离线") + "§a]"));
        info(color("§8========================"));
        line("UUID", d.uuid != null && !d.uuid.isEmpty() ? d.uuid : "未知", Formatting.WHITE);
        line("状态", online ? "在线" : "离线", online ? Formatting.GREEN : Formatting.RED);
        line("首次加入", fmtTime(d.firstLogin), Formatting.WHITE);
        line("最近上线", fmtTime(d.lastLogin), Formatting.WHITE);
        if (!online) line("最近下线", fmtTime(d.lastLogout), Formatting.WHITE);
        double avg = avgDaily(d);
        line("总在线", fmtDur(d.duration), Formatting.YELLOW);
        line("近一月", fmtDur(d.monthDuration), Formatting.YELLOW);
        line("日均", String.format("%.1f h", avg), Formatting.YELLOW);
        double kd = d.deaths > 0 ? (double) d.kills / d.deaths : (double) d.kills;
        line("击杀/死亡", d.kills + " / " + d.deaths + "  (KD " + String.format("%.2f", kd) + ")", Formatting.RED);
        line("发言数", String.valueOf(d.chats), Formatting.WHITE);
        if (d.notes != null && !d.notes.isEmpty()) line("备注", d.notes, Formatting.WHITE);
        info(color("§8========================"));
    }

    /** 单行: §8| §7标签(对齐) §值(彩色) */
    private void line(String label, String value, Formatting valueColor) {
        Character vc = valueColor.getCode();
        info(color("§8| §7" + label + pad(label) + "§" + vc + value));
    }

    /** 粗略按显示宽度补空格对齐标签（CJK 计 2 宽，ASCII 计 1 宽），目标宽度 10。 */
    private static String pad(String label) {
        int w = 0;
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            w += (c >= 0x4E00 && c <= 0x9FFF) ? 2 : 1;
        }
        return " ".repeat(Math.max(0, 10 - w));
    }

    // ---- 文本/时间/时长格式化 ----

    private static double avgDaily(PlayerData d) {
        double days = Math.max((d.lastLogout - d.firstLogin) / 86400.0, 1.0);
        return (d.duration / 3600.0) / days;
    }

    private static String fmtTime(long ts) {
        if (ts <= 0) return "未知";
        return FMT.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), TZ));
    }

    private static String fmtDur(long sec) {
        if (sec < 0) sec = 0;
        long d = sec / 86400;
        long h = (sec % 86400) / 3600;
        long m = (sec % 3600) / 60;
        if (d > 0) return d + "天" + h + "小时";
        if (h > 0) return h + "小时" + m + "分";
        return m + "分";
    }

    private static final Map<Character, Formatting> CODE_MAP = new HashMap<>();
    static {
        for (Formatting f : Formatting.values()) {
            Character code = f.getCode();
            if (code != null) CODE_MAP.put(Character.toLowerCase(code), f);
        }
    }

    /** 把含 § 颜色码的字符串转换为带格式的 Text 组件（每段仅应用最近一次颜色/格式）。 */
    private static Text color(String s) {
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

    // ---- 极简 JSON 字段提取（针对 /api/getPlayer 的固定结构，零依赖） ----

    private static String readString(String json, String key) {
        String k = "\"" + key + "\"";
        int idx = json.indexOf(k);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + k.length());
        if (colon < 0) return null;
        int q = json.indexOf('"', colon + 1);
        if (q < 0) return null;
        StringBuilder sb = new StringBuilder();
        int i = q + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                if (i + 1 >= json.length()) break;
                char n = json.charAt(i + 1);
                switch (n) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'u' -> {
                        if (i + 5 < json.length()) {
                            sb.append((char) Integer.parseInt(json.substring(i + 2, i + 6), 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(n);
                }
                i += 2;
                continue;
            }
            if (c == '"') break;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static long readLong(String json, String key) {
        String k = "\"" + key + "\"";
        int idx = json.indexOf(k);
        if (idx < 0) return 0;
        int colon = json.indexOf(':', idx + k.length());
        if (colon < 0) return 0;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        int start = i;
        while (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '-')) i++;
        if (i == start) return 0;
        try {
            return Long.parseLong(json.substring(start, i));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class PlayerData {
        String uuid, name, notes;
        long firstLogin, lastLogin, lastLogout, duration, monthDuration, kills, deaths, chats;

        static PlayerData parse(String json) throws Exception {
            long code = readLong(json, "code");
            if (code != 200) {
                String msg = readString(json, "msg");
                throw new Exception(msg != null ? msg : "未知错误 (code=" + code + ")");
            }
            PlayerData d = new PlayerData();
            d.uuid = readString(json, "uuid");
            d.name = readString(json, "name");
            d.firstLogin = readLong(json, "firstLoginTime");
            d.lastLogin = readLong(json, "lastLoginTime");
            d.lastLogout = readLong(json, "lastLogoutTime");
            d.duration = readLong(json, "duration");
            d.monthDuration = readLong(json, "month_duration");
            d.kills = readLong(json, "c_kill");
            d.deaths = readLong(json, "c_death");
            d.chats = readLong(json, "c_chat");
            d.notes = readString(json, "notes");
            return d;
        }
    }
}
