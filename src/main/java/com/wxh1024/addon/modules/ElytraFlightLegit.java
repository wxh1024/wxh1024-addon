package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.systems.modules.Module;

/**
 * 合法鞘翅飞行（从 SlimefunHelper ElytraFlightLegit 重写）
 * 原版逻辑已被注释，仅保留 enable 与 isEnableLegitFly 判定
 */
public class ElytraFlightLegit extends Module {
    public static ElytraFlightLegit INSTANCE;

    public ElytraFlightLegit() {
        super(Wxh1024Addon.WXH1024, "ElytraFlightLegit", "合法鞘翅飞行（预留）");
        INSTANCE = this;
    }

    /**
     * 是否应该启用合法飞行：本模块启用且 ElytraFlight 未启用
     */
    public boolean isEnableLegitFly() {
        return isActive() && !ElytraFlight.INSTANCE.isActive();
    }
}
