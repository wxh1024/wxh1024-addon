package com.wxh1024.addon.utils.elytra;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * 鞘翅飞行所需的速度/旋转数学工具
 */
public class EntityUtils {
    /** 输入向量按 yaw 转向世界速度 */
    public static Vec3d movementInputToVelocity(Vec3d input, float speed, float yaw) {
        double d = input.lengthSquared();
        if (d < 1.0E-7) return Vec3d.ZERO;
        Vec3d v = (d > 1 ? input.normalize() : input).multiply(speed);
        float f = MathHelper.sin(yaw * 0.017453292F);
        float g = MathHelper.cos(yaw * 0.017453292F);
        return new Vec3d(v.x * g - v.z * f, v.y, v.z * g + v.x * f);
    }

    /** 由视角与横/纵/垂直输入合成世界速度向量 */
    public static Vec3d lookCoordToPos(float pitch, float yaw, double sideways, double vertical, double forward) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        double cosY = MathHelper.cos(g), sinY = MathHelper.sin(g);
        double cosP = MathHelper.cos(f), sinP = MathHelper.sin(f);
        double fx = forward * cosP;
        double fy = -forward * sinP;
        double x = sideways * cosY + fx * sinY;
        double z = -sideways * sinY + fx * cosY;
        return new Vec3d(x, fy + vertical, z);
    }

    public static Vec2f rotationToPitchYaw(Vec3d vec) {
        double len = vec.horizontalLength();
        if (len < 1.0E-6) return new Vec2f(0, 0);
        float pitch = (float) Math.toDegrees(Math.atan2(-vec.y, len));
        float yaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
        return new Vec2f(pitch, yaw);
    }

    public static float rotationToPitch(Vec3d vec) {
        double len = vec.horizontalLength();
        if (len < 1.0E-6) return 0;
        return (float) Math.toDegrees(Math.atan2(-vec.y, len));
    }

    /** pitch/yaw -> look 向量 */
    public static Vec3d pitchYawToRotation(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        double cosP = MathHelper.cos(f), sinP = MathHelper.sin(f);
        double cosY = MathHelper.cos(g), sinY = MathHelper.sin(g);
        return new Vec3d(cosY * cosP, -sinP, sinY * cosP);
    }

    /** 有效重力（简化版，忽略方块下 0.04 加成） */
    public static double getEffectiveGravity(Entity entity) {
        return entity.hasNoGravity() ? 0 : 0.08;
    }

    /** 滑翔一 tick 后的速度（简化版，仅做方向转向与重力修正） */
    public static Vec3d calculateGlidingVelocity(Entity entity, Vec3d lastVelocity, Vec3d look, boolean applyGravity) {
        Vec3d v = lastVelocity;
        if (applyGravity) v = v.add(0, -getEffectiveGravity(entity), 0);
        Vec3d forward = look.normalize();
        // 滑翔转向：速度向视线方向轻微偏转
        double dot = v.dotProduct(forward);
        if (dot < 0) return v.multiply(0.99).add(forward.multiply(0.01 * -dot));
        return v;
    }
}
