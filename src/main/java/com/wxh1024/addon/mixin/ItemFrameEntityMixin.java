package com.wxh1024.addon.mixin;

import com.wxh1024.addon.modules.ItemFrameChestOpener;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {

    /** 拦截 interact(Player, Hand) */
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interactHand(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        handle(player, hand, cir);
    }

    /** 拦截 interact(Player, Vec3d, Hand) — 带点击位置的交互（主入口） */
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interactAt(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        handle(player, hand, cir);
    }

    private void handle(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemFrameChestOpener mod = Modules.get().get(ItemFrameChestOpener.class);
        if (mod == null || !mod.isActive()) return;

        if (hand == Hand.MAIN_HAND) mod.tryOpen((ItemFrameEntity) (Object) this);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
