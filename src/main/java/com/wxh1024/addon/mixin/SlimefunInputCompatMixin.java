package com.wxh1024.addon.mixin;

import com.wxh1024.addon.utils.ControlifyBindings;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

/**
 * PlayerInputUtils.of(GameOptions) → Controlify 绑定 || KeyBinding.isPressed()。
 */
@Pseudo
@Mixin(targets = "me.matl114.utils.entity.PlayerInputUtils", remap = false)
public abstract class SlimefunInputCompatMixin {

    private static volatile Constructor<?> inputCtor;

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
        if (!ControlifyBindings.available()) return;

        Constructor<?> ctor = getCtor();
        if (ctor == null) return;

        try {
            Object result = ctor.newInstance(
                ControlifyBindings.isWalkForward()  || mc.options.forwardKey.isPressed(),
                ControlifyBindings.isWalkBackward() || mc.options.backKey.isPressed(),
                ControlifyBindings.isWalkLeft()     || mc.options.leftKey.isPressed(),
                ControlifyBindings.isWalkRight()    || mc.options.rightKey.isPressed(),
                ControlifyBindings.isJump()         || mc.options.jumpKey.isPressed(),
                ControlifyBindings.isSneak()        || mc.options.sneakKey.isPressed(),
                ControlifyBindings.isSprint()       || mc.options.sprintKey.isPressed()
            );
            cir.setReturnValue(result);
        } catch (Exception ignored) {
        }
    }

    private static Constructor<?> getCtor() {
        if (inputCtor != null) return inputCtor;
        try {
            inputCtor = Class.forName("me.matl114.utils.entity.PlayerInputUtils$Input")
                .getConstructor(boolean.class,boolean.class,boolean.class,boolean.class,
                                boolean.class,boolean.class,boolean.class);
        } catch (Exception ignored) {
        }
        return inputCtor;
    }
}
