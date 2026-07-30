package com.wxh1024.addon.mixin;

import com.wxh1024.addon.utils.ControlifyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * KeyBinding.isPressed() → Controlify 绑定补位。
 */
@Mixin(KeyBinding.class)
public abstract class KeyBindingCompatMixin {

    @Inject(method = "isPressed", at = @At("RETURN"), cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (!ControlifyBindings.enabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) return;

        KeyBinding self = (KeyBinding) (Object) this;
        boolean result = false;

        if      (self == mc.options.forwardKey) result = ControlifyBindings.isWalkForward();
        else if (self == mc.options.backKey)    result = ControlifyBindings.isWalkBackward();
        else if (self == mc.options.leftKey)    result = ControlifyBindings.isWalkLeft();
        else if (self == mc.options.rightKey)   result = ControlifyBindings.isWalkRight();
        else if (self == mc.options.jumpKey)    result = ControlifyBindings.isJump();
        else if (self == mc.options.sneakKey)   result = ControlifyBindings.isSneak();
        else if (self == mc.options.sprintKey)  result = ControlifyBindings.isSprint();

        if (result) cir.setReturnValue(true);
    }
}
