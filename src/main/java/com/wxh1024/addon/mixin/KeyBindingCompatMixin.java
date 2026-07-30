package com.wxh1024.addon.mixin;

import com.wxh1024.addon.utils.ControlifyBindings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class KeyBindingCompatMixin {

    @Inject(method = "isPressed", at = @At("RETURN"), cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (!ControlifyBindings.enabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        KeyBinding self = (KeyBinding) (Object) this;
        GameOptions o = mc.options;
        int idx = -1;
        if      (self == o.forwardKey) idx = 0;
        else if (self == o.backKey)    idx = 1;
        else if (self == o.leftKey)    idx = 2;
        else if (self == o.rightKey)   idx = 3;
        else if (self == o.jumpKey)    idx = 4;
        else if (self == o.sneakKey)   idx = 5;
        else if (self == o.sprintKey)  idx = 6;
        else return;

        if (ControlifyBindings.is(idx)) cir.setReturnValue(true);
    }
}
