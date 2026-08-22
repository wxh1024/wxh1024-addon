package com.wxh1024.addon.utils.elytra;

import net.minecraft.util.math.Vec3d;

/**
 * 鞘翅相关模块之间共享的运行时状态
 */
public class ElytraShared {
    /** 当前 tick 是否处于 Grim 漂浮状态（速度为零且需保持静止） */
    public static boolean grimFloatingTick = false;
    /** 上一 tick 玩家是否具有地面支撑 */
    public static boolean lastHasGroundSupport = false;
    /** 上一 tick 玩家是否在水中/岩浆中 */
    public static boolean lastInWater = false;
    public static boolean lastInLava = false;
    /** 上一 tick 已知速度 */
    public static Vec3d lastKnownClientVelocity = Vec3d.ZERO;
}
