package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import com.wxh1024.addon.utils.elytra.ElytraShared;
import com.wxh1024.addon.utils.elytra.EntityUtils;
import com.wxh1024.addon.utils.elytra.MotionMode;
import com.wxh1024.addon.utils.elytra.PlayerInput;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * 简易鞘翅飞行控制（从 SlimefunHelper ElytraFlight 重写）
 */
public class ElytraFlight extends Module {
    public static ElytraFlight INSTANCE;

    private final SettingGroup sgControl = settings.createGroup("控制");
    private final SettingGroup sgAutoFly = settings.createGroup("自动飞行");

    public final Setting<Mode> controlMode = sgControl.add(new EnumSetting.Builder<Mode>()
        .name("飞行模式")
        .description("速度控制模式")
        .defaultValue(Mode.CONTROL)
        .build()
    );

    public final Setting<MotionMode> motionMode = sgControl.add(new EnumSetting.Builder<MotionMode>()
        .name("运动模式")
        .description("运动控制方式")
        .defaultValue(MotionMode.VOID)
        .build()
    );

    public final Setting<Keybind> modeSwitchKey = sgControl.add(new KeybindSetting.Builder()
        .name("模式切换键")
        .description("按下后切换飞行模式")
        .defaultValue(Keybind.none())
        .action(this::toggleMode)
        .build()
    );

    public final Setting<Double> packetMotion = sgControl.add(new DoubleSetting.Builder()
        .name("速度量")
        .description("每 tick 施加的移动量")
        .defaultValue(0.05)
        .min(0)
        .max(10000)
        .build()
    );

    public final Setting<Double> motionArg = sgControl.add(new DoubleSetting.Builder()
        .name("速度插值参数")
        .description("当前速度与目标速度的插值系数")
        .defaultValue(1.0)
        .min(0)
        .max(1)
        .build()
    );

    public final Setting<Boolean> motionArgLerpStarting = sgControl.add(new BoolSetting.Builder()
        .name("初始即插值")
        .description("从启动时就启用速度插值")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> horizontalFlyNoGravity = sgControl.add(new BoolSetting.Builder()
        .name("水平无重力")
        .description("水平飞行时移除重力")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> useFloatingUtils = sgControl.add(new BoolSetting.Builder()
        .name("使用漂浮工具")
        .description("速度为 0 时使用漂浮状态保持静止")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> takeOffOptimizeTicks = sgControl.add(new IntSetting.Builder()
        .name("按住跳跃起飞")
        .description("按住跳跃达到该 tick 数后自动起飞（0 禁用）")
        .defaultValue(0)
        .min(0)
        .max(100)
        .build()
    );

    public final Setting<Boolean> landAutoClose = sgControl.add(new BoolSetting.Builder()
        .name("落地自动关闭")
        .description("飞行结束后落地自动关闭本模块")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> useAutoRescale = sgControl.add(new BoolSetting.Builder()
        .name("自动微调视角")
        .description("周期性微调视角以规避 Grim 旋转检测")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> autoFly = sgAutoFly.add(new BoolSetting.Builder()
        .name("自动飞行")
        .description("无输入时沿视线方向持续飞行")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> autoFlyAutoJumpOff = sgAutoFly.add(new BoolSetting.Builder()
        .name("自动飞行自动起飞")
        .description("自动飞行模式下自动打开鞘翅")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> autoFlyLandAutoClose = sgAutoFly.add(new BoolSetting.Builder()
        .name("自动飞行落地关闭")
        .description("自动飞行落地后关闭自动飞行")
        .defaultValue(false)
        .build()
    );

    private boolean currentTakeOff;
    private Vec3d lastVelocity;
    private int holdJumpCounter;
    private Boolean modifyNoGravity;

    public ElytraFlight() {
        super(Wxh1024Addon.WXH1024, "ElytraFlight", "简易鞘翅飞行控制，支持加速/俯冲/自动飞行");
        INSTANCE = this;
    }

    public enum Mode {
        CONTROL,
        ROTATION
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        // 记录是否经历过飞行（用于落地判断）
        if (player.isGliding()) currentTakeOff = true;
        if (mc.options.jumpKey.isPressed()) holdJumpCounter++;
        else holdJumpCounter = 0;

        if (!isActive()) {
            lastVelocity = null;
            return;
        }

        // 按住跳跃起飞优化
        if (!player.isGliding() && takeOffOptimizeTicks.get() > 0 && holdJumpCounter >= takeOffOptimizeTicks.get()) {
            if (!player.isOnGround() && player.checkGliding() && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        if (player.isGliding()) {
            if (lastVelocity == null) lastVelocity = Vec3d.ZERO;

            // GrimAccelerate 开启时本模块不干预
            if (Modules.get().isActive(ElytraGrimAccelerate.class)) {
                handleLanding(player);
                return;
            }

            Vec3d controlMotion = Vec3d.ZERO;
            boolean shouldControl = false;
            boolean shouldCheckRocket = false;
            double motionAmount = packetMotion.get();
            PlayerInput input = PlayerInput.of(mc.options);

            switch (controlMode.get()) {
                case CONTROL -> {
                    boolean packetMotionEnabled = true;
                    Vec3d movementInput = new Vec3d(input.sidewaysSpeed(), input.upwardSpeed(), input.forwardSpeed());
                    Vec3d velocity = EntityUtils.movementInputToVelocity(movementInput, 1.0F, player.getYaw());
                    if (motionMode.get() == MotionMode.FIRE_WORKS) {
                        packetMotionEnabled = false;
                        shouldCheckRocket = true;
                        if (ElytraExtra.INSTANCE.canFireworkControlMotion()) packetMotionEnabled = true;
                    }
                    controlMotion = velocity;
                    shouldControl = packetMotionEnabled;
                }
                case ROTATION -> {
                    boolean packetMotionEnabled = true;
                    Vec3d velocity = EntityUtils.lookCoordToPos(
                        player.getPitch(), player.getYaw(), input.sidewaysSpeed(), 0, input.forwardSpeed());
                    Vec3d vertical = new Vec3d(0, input.upwardSpeed(), 0);
                    velocity = velocity.add(vertical);
                    if (motionMode.get() == MotionMode.FIRE_WORKS) {
                        packetMotionEnabled = false;
                        shouldCheckRocket = true;
                        if (ElytraExtra.INSTANCE.canFireworkControlMotion()) packetMotionEnabled = true;
                    }
                    controlMotion = velocity;
                    shouldControl = packetMotionEnabled;
                }
            }

            // 自动飞行：无输入时沿视线方向
            if (controlMotion.lengthSquared() < 1.0E-6 && autoFly.get()) {
                controlMotion = player.getRotationVector();
            }

            Vec3d wayVector = controlMotion.normalize();
            Vec3d realVector = wayVector.multiply(motionAmount);

            // 速度插值
            double a = motionArg.get();
            if (a != 1.0
                && (motionArgLerpStarting.get() || lastVelocity.lengthSquared() > 0.01 * motionAmount * motionAmount)) {
                realVector = realVector.multiply(a).add(lastVelocity.multiply(1.0 - a));
                if (wayVector.lengthSquared() > 1.0E-6) {
                    realVector = realVector.normalize().multiply(motionAmount);
                }
            }
            lastVelocity = realVector;
            if (lastVelocity.length() < 0.1 * motionAmount) lastVelocity = Vec3d.ZERO;

            // 漂浮工具处理（Grim 静止）
            if (ElytraShared.grimFloatingTick) {
                realVector = Vec3d.ZERO;
                shouldControl = true;
                shouldCheckRocket = false;
            } else if (useFloatingUtils.get() && realVector.lengthSquared() < 1.0E-4) {
                if (!ElytraExtra.INSTANCE.canFireworkControlMotion()) {
                    if (ElytraExtra.INSTANCE.shouldApplyOnGroundFly() && ElytraShared.lastHasGroundSupport) {
                        shouldControl = true;
                        shouldCheckRocket = false;
                    } else {
                        realVector = Vec3d.ZERO;
                        ElytraShared.grimFloatingTick = true;
                        shouldControl = true;
                        shouldCheckRocket = false;
                    }
                } else {
                    shouldControl = true;
                    shouldCheckRocket = false;
                }
            }

            // 贴地飞行时去除向下速度
            if (ElytraExtra.INSTANCE.shouldApplyOnGroundFly()
                && ElytraShared.lastHasGroundSupport
                && realVector.y < 0) {
                realVector = realVector.withAxis(Direction.Axis.Y, 0);
            }

            // 烟花模式下的旋转控制
            if (motionMode.get() == MotionMode.FIRE_WORKS && realVector.lengthSquared() > 5.0E-3) {
                if (realVector.horizontalLengthSquared() > 5.0E-3) {
                    Vec2f py = EntityUtils.rotationToPitchYaw(realVector.normalize());
                    player.setPitch(py.x);
                    player.setYaw(py.y);
                } else {
                    player.setPitch(EntityUtils.rotationToPitch(realVector.normalize()));
                }
                // 周期性微调 yaw 以触发服务器旋转更新
                if (useAutoRescale.get()) {
                    float yaw = player.getYaw();
                    player.setYaw(mc.world.getTime() % 2 == 0 ? yaw + 0.01F : yaw - 0.01F);
                }
            }

            if (shouldControl) {
                if (Math.abs(realVector.y) <= 1.0E-2 && horizontalFlyNoGravity.get() && !player.isOnGround()) {
                    modifyNoGravity = player.hasNoGravity();
                    player.setNoGravity(true);
                }
                player.setVelocity(
                    (motionMode.get() == MotionMode.FIRE_WORKS && useAutoRescale.get())
                        ? ElytraExtra.INSTANCE.applyAxisLimit(realVector, player.getRotationVector(), !player.hasNoGravity())
                        : realVector
                );
            }

            if (shouldCheckRocket) {
                ElytraExtra.INSTANCE.launchFirework(player.getPitch(), player.getYaw());
            }
        } else {
            lastVelocity = null;
            if (autoFly.get() && autoFlyAutoJumpOff.get()) {
                ElytraExtra.INSTANCE.autoTakeoff();
            }
        }

        handleLanding(player);

        // 恢复重力状态
        if (modifyNoGravity != null) {
            player.setNoGravity(modifyNoGravity);
            modifyNoGravity = null;
        }
    }

    private void handleLanding(ClientPlayerEntity player) {
        if (currentTakeOff && !player.isGliding() && player.isOnGround()) {
            if (landAutoClose.get()) toggle();
            if (autoFly.get() && autoFlyLandAutoClose.get()) autoFly.set(false);
            currentTakeOff = false;
        }
    }

    private void toggleMode() {
        Mode[] values = Mode.values();
        controlMode.set(values[(controlMode.get().ordinal() + 1) % values.length]);
        info("模式切换为: " + controlMode.get().name());
    }

    @Override
    public String getInfoString() {
        return controlMode.get().name() + " " + motionMode.get().name();
    }
}
