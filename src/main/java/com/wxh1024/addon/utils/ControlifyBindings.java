package com.wxh1024.addon.utils;

import com.wxh1024.addon.modules.ControlifyCompat;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Method;

/**
 * Controlify 绑定缓存 + 开关状态。
 */
public final class ControlifyBindings {

    private static volatile boolean ready;
    private static volatile boolean failed;
    private static volatile int lastNullTick = -1;
    private static volatile Method digitalNow;
    private static volatile Object fwd, back, left, right, jump, sneak, sprint;

    // 模块开关缓存（每 tick 查一次）
    private static volatile boolean moduleOn;
    private static volatile int moduleCheckedTick = -1;

    private ControlifyBindings() {}

    /** 检查是否启用：Controlify 可用 + 模块开关开启 */
    public static boolean enabled() {
        if (!available()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        int tick = mc.player.age;
        if (tick == moduleCheckedTick) return moduleOn;
        moduleCheckedTick = tick;
        try {
            ControlifyCompat mod = Modules.get().get(ControlifyCompat.class);
            moduleOn = mod != null && mod.isActive();
        } catch (Exception e) {
            moduleOn = false;
        }
        return moduleOn;
    }

    private static boolean available() {
        if (ready) return true;
        if (failed) return false;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                if (mc.player.age == lastNullTick) return false;
                boolean ok = init();
                if (!ok) lastNullTick = mc.player.age;
                return ok;
            }
        } catch (Exception ignored) {}
        return init();
    }

    public static boolean isWalkForward()  { return pad(fwd); }
    public static boolean isWalkBackward() { return pad(back); }
    public static boolean isWalkLeft()     { return pad(left); }
    public static boolean isWalkRight()    { return pad(right); }
    public static boolean isJump()         { return pad(jump); }
    public static boolean isSneak()        { return pad(sneak); }
    public static boolean isSprint()       { return pad(sprint); }

    private static boolean pad(Object binding) {
        try { return binding != null && (boolean) digitalNow.invoke(binding); }
        catch (Exception e) { return false; }
    }

    private static synchronized boolean init() {
        if (ready) return true;
        if (failed) return false;
        try {
            Class<?> cf = Class.forName("dev.isxander.controlify.Controlify");
            Object inst = cf.getMethod("instance").invoke(null);
            Object opt = cf.getMethod("getCurrentController").invoke(inst);
            Object ctrl = opt.getClass().getMethod("orElse", Object.class).invoke(opt, (Object) null);
            if (ctrl == null) return false;

            Class<?> b = Class.forName("dev.isxander.controlify.bindings.ControlifyBindings");
            Method on = null;
            Object[] out = new Object[7];
            String[] names = {"WALK_FORWARD","WALK_BACKWARD","WALK_LEFT","WALK_RIGHT",
                              "JUMP","SNEAK","SPRINT"};
            for (int i = 0; i < 7; i++) {
                Object sup = b.getField(names[i]).get(null);
                if (on == null) on = sup.getClass().getMethod("on", ctrl.getClass());
                out[i] = on.invoke(sup, ctrl);
            }
            fwd=out[0]; back=out[1]; left=out[2]; right=out[3];
            jump=out[4]; sneak=out[5]; sprint=out[6];
            digitalNow = out[0].getClass().getMethod("digitalNow");
            ready = true;
            return true;
        } catch (Exception e) {
            failed = true;
            return false;
        }
    }
}
