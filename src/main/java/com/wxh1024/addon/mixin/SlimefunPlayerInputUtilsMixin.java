package com.wxh1024.addon.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

/**
 * 仅拦截 SlimefunHelper 的 PlayerInputUtils.of(GameOptions)，
 * 从 playerInput 读取（Controlify 手柄写入的目标）构造 Input。
 *
 * 为什么这时 readerInput 是当前帧最新值：
 * SlimefunHelper 模块在 PlayerMoveEvent / MovementModifier 中调用 of(GameOptions)，
 * 这些都在 ClientPlayerEntity.tickMovement() 之后触发，
 * 此时 DualInput.tick() 已合并完毕，playerInput 是最新状态。
 */
@Pseudo
@Mixin(targets = "me.matl114.utils.entity.PlayerInputUtils", remap = false)
public abstract class SlimefunPlayerInputUtilsMixin {

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

        PlayerInput pi = mc.player.input.playerInput;
        if (pi == null) return;

        Constructor<?> ctor = getConstructor();
        if (ctor == null) return;

        try {
            Object result = ctor.newInstance(
                pi.forward(), pi.backward(), pi.left(), pi.right(),
                pi.jump(), pi.sneak(), pi.sprint()
            );
            cir.setReturnValue(result);
        } catch (Exception ignored) {
        }
    }

    private static Constructor<?> getConstructor() {
        if (inputCtor != null) return inputCtor;
        try {
            Class<?> c = Class.forName("me.matl114.utils.entity.PlayerInputUtils$Input");
            inputCtor = c.getConstructor(
                boolean.class, boolean.class, boolean.class, boolean.class,
                boolean.class, boolean.class, boolean.class);
        } catch (Exception ignored) {
        }
        return inputCtor;
    }
}
