package com.wxh1024.addon.utils.elytra;

import net.minecraft.client.option.GameOptions;

/**
 * 简化版的玩家输入读取（与 SlimefunHelper 的 PlayerInputUtils 对应）
 */
public record PlayerInput(double sidewaysSpeed, double upwardSpeed, double forwardSpeed) {
    public static PlayerInput of(GameOptions options) {
        double sideways = 0;
        double forward = 0;
        double upward = 0;

        if (options.leftKey.isPressed()) sideways += 1;
        if (options.rightKey.isPressed()) sideways -= 1;
        if (options.forwardKey.isPressed()) forward += 1;
        if (options.backKey.isPressed()) forward -= 1;
        if (options.jumpKey.isPressed()) upward += 1;
        if (options.sneakKey.isPressed()) upward -= 1;

        return new PlayerInput(sideways, upward, forward);
    }

    public boolean hasMovementControl() {
        return sidewaysSpeed != 0 || upwardSpeed != 0 || forwardSpeed != 0;
    }
}
