package com.wxh1024.addon.utils;

import com.wxh1024.addon.modules.ControlifyCompat;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Method;

public final class ControlifyBindings {

    private static volatile boolean ready, failed, noController;
    private static volatile Method digitalNow;
    private static volatile Object[] bindings; // [fwd,back,left,right,jump,sneak,sprint]
    private static volatile Object instance;
    private static volatile Class<?> bindingsClass;
    static volatile boolean moduleOn;
    static volatile int checkTick = -1;

    private ControlifyBindings() {}

    private static volatile long nextTry;

    public static boolean enabled() {
        if (ready) return checkModule();
        if (failed || noController) {
            long now = System.currentTimeMillis();
            if (now < nextTry) return false;
            nextTry = now + 5000; // 5 秒后重试（可能玩家刚接上手柄）
        }
        return init();
    }

    public static boolean is(int idx) {
        if (bindings == null || idx < 0 || idx >= bindings.length) return false;
        Object b = bindings[idx];
        if (b == null) return false;
        try { return (boolean) digitalNow.invoke(b); }
        catch (Throwable e) { return false; }
    }

    private static boolean checkModule() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        int tick = mc.player.age;
        if (tick == checkTick) return moduleOn;
        checkTick = tick;
        ControlifyCompat mod = Modules.get().get(ControlifyCompat.class);
        return moduleOn = (mod != null && mod.isActive());
    }

    private static synchronized boolean init() {
        if (ready) return checkModule();
        if (failed) return false;
        try {
            if (instance == null) {
                Class<?> cf = Class.forName("dev.isxander.controlify.Controlify");
                instance = cf.getMethod("instance").invoke(null);
                bindingsClass = Class.forName("dev.isxander.controlify.bindings.ControlifyBindings");
            }
            Object opt = instance.getClass().getMethod("getCurrentController").invoke(instance);
            Object ctrl = opt.getClass().getMethod("orElse", Object.class).invoke(opt, (Object) null);
            if (ctrl == null) { noController = true; return false; }
            noController = false;

            Method on = null;
            bindings = new Object[7];
            String[] names = {"WALK_FORWARD","WALK_BACKWARD","WALK_LEFT","WALK_RIGHT",
                              "JUMP","SNEAK","SPRINT"};
            for (int i = 0; i < 7; i++) {
                Object sup = bindingsClass.getField(names[i]).get(null);
                if (on == null) on = sup.getClass().getMethod("on", ctrl.getClass());
                bindings[i] = on.invoke(sup, ctrl);
            }
            digitalNow = bindings[0].getClass().getMethod("digitalNow");
            ready = true;
            return checkModule();
        } catch (Exception e) {
            failed = true;
            return false;
        }
    }
}
