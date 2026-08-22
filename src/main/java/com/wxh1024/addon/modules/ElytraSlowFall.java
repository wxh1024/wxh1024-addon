package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * 鞘翅缓降（从 SlimefunHelper ElytraSlowFall 重写）
 * 通过将俯仰角置 0 并周期性翻转 180 度来合法降低下落速度
 */
public class ElytraSlowFall extends Module {
    public static ElytraSlowFall INSTANCE;

    public ElytraSlowFall() {
        super(Wxh1024Addon.WXH1024, "ElytraSlowFall", "鞘翅缓降，降低下落速度");
        INSTANCE = this;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        if (player.isGliding() && !player.isOnGround()) {
            boolean rotateYaw = mc.world.getTime() % 2 == 0;
            player.setPitch(0.0F);
            if (rotateYaw) {
                player.setYaw(player.getYaw() + 180);
            }
        }
    }
}
