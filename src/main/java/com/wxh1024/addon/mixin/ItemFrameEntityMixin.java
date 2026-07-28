package com.wxh1024.addon.mixin;

import com.wxh1024.addon.modules.ItemFrameChestOpener;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {

    /** 拦截 interact（展示框唯一交互入口，覆盖旋转/放置等所有交互） */
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemFrameChestOpener mod = Modules.get().get(ItemFrameChestOpener.class);
        if (mod == null || !mod.isActive()) return;

        if (hand == Hand.MAIN_HAND) mod.tryOpen((ItemFrameEntity) (Object) this);
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
