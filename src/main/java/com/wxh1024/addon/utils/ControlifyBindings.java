package com.wxh1024.addon.utils;

import com.wxh1024.addon.modules.ControlifyCompat;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Method;

public final class ControlifyBindings {

    private static volatile boolean ready, failed;
    private static volatile Method digitalNow;
    private static volatile Object[] bindings; // [fwd,back,left,right,jump,sneak,sprint]
    static volatile boolean moduleOn;
    static volatile int checkTick = -1;

    private ControlifyBindings() {}

    public static boolean enabled() {
        if (ready) return checkModule();
        return init();
    }

    public static boolean is(int idx) {
        try { return (boolean) digitalNow.invoke(bindings[idx]); }
        catch (Exception e) { return false; }
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
            Class<?> cf = Class.forName("dev.isxander.controlify.Controlify");
            Object inst = cf.getMethod("instance").invoke(null);
            Object opt = cf.getMethod("getCurrentController").invoke(inst);
            Object ctrl = opt.getClass().getMethod("orElse", Object.class).invoke(opt, (Object) null);
            if (ctrl == null) return false;

            Class<?> b = Class.forName("dev.isxander.controlify.bindings.ControlifyBindings");
            Method on = null;
            bindings = new Object[7];
            String[] names = {"WALK_FORWARD","WALK_BACKWARD","WALK_LEFT","WALK_RIGHT",
                              "JUMP","SNEAK","SPRINT"};
            for (int i = 0; i < 7; i++) {
                Object sup = b.getField(names[i]).get(null);
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
