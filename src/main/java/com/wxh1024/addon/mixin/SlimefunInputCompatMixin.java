package com.wxh1024.addon.mixin;

import com.wxh1024.addon.utils.ControlifyBindings;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Pseudo
@Mixin(targets = "me.matl114.utils.entity.PlayerInputUtils", remap = false)
public abstract class SlimefunInputCompatMixin {

    private static volatile Constructor<?> ctor;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(
        method = "of(Lnet/minecraft/class_315;)Lme/matl114/utils/entity/PlayerInputUtils$Input;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void onOfGameOptions(CallbackInfoReturnable cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.input == null) return;
        if (!ControlifyBindings.enabled()) return;

        Constructor<?> c = getCtor();
        if (c == null) return;

        var o = mc.options;
        try {
            cir.setReturnValue(c.newInstance(
                ControlifyBindings.is(0) || o.forwardKey.isPressed(),
                ControlifyBindings.is(1) || o.backKey.isPressed(),
                ControlifyBindings.is(2) || o.leftKey.isPressed(),
                ControlifyBindings.is(3) || o.rightKey.isPressed(),
                ControlifyBindings.is(4) || o.jumpKey.isPressed(),
                ControlifyBindings.is(5) || o.sneakKey.isPressed(),
                ControlifyBindings.is(6) || o.sprintKey.isPressed()
            ));
        } catch (Exception ignored) {}
    }

    private static Constructor<?> getCtor() {
        if (ctor != null) return ctor;
        try {
            ctor = Class.forName("me.matl114.utils.entity.PlayerInputUtils$Input")
                .getConstructor(boolean.class,boolean.class,boolean.class,boolean.class,
                                boolean.class,boolean.class,boolean.class);
        } catch (Exception ignored) {}
        return ctor;
    }
}
