package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;

/**
 * 坏数据包修复（从 SlimefunHelper BadPacketsFix 重写）
 * 过滤重复的疾跑/输入/旋转包，修复服务端非法视距等
 */
public class BadPacketsFix extends Module {
    public static BadPacketsFix INSTANCE;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> enableSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("修复重复疾跑")
        .description("过滤与服务器状态一致的疾跑命令包")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableInput = sgGeneral.add(new BoolSetting.Builder()
        .name("修复重复输入")
        .description("过滤与上次发送相同的输入包")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableFly = sgGeneral.add(new BoolSetting.Builder()
        .name("修复飞行包")
        .description("服务器不允许飞行时过滤飞行状态包")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> exemptDupRot = sgGeneral.add(new BoolSetting.Builder()
        .name("豁免完整旋转")
        .description("位置+旋转完整包不再做去重过滤")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableRot = sgGeneral.add(new BoolSetting.Builder()
        .name("修复重复旋转")
        .description("过滤与上次相同的旋转包")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableFullRot = sgGeneral.add(new BoolSetting.Builder()
        .name("过滤完整包旋转")
        .description("完整包中的重复旋转也一并移除")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> enableViewDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("修复非法视距")
        .description("服务端下发超过 32 的视距时强制修正")
        .defaultValue(true)
        .build()
    );

    private boolean serverSprint;
    private boolean serverCanFly;
    private float serverPitch;
    private float serverYaw;
    private boolean exempt;
    private boolean lastJump, lastSneak, lastSprint;
    private float lastForwardInput, lastSidewaysInput;

    public BadPacketsFix() {
        super(Wxh1024Addon.WXH1024, "BadPacketsFix", "过滤重复/非法数据包，规避 Grim 检测");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            serverSprint = mc.player.isSprinting();
            serverCanFly = mc.player.getAbilities().allowFlying;
            serverPitch = mc.player.getPitch();
            serverYaw = mc.player.getYaw();
        }
        exempt = false;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ClientCommandC2SPacket command) {
            if (command.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING
                || command.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                boolean starting = command.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING;
                if (serverSprint == starting) {
                    if (enableSprint.get()) event.cancel();
                } else {
                    serverSprint = starting;
                }
            }
        } else if (event.packet instanceof PlayerInputC2SPacket input) {
            if (mc.player != null && !mc.player.hasVehicle() && enableInput.get()) {
                boolean forward = input.input().forward();
                boolean backward = input.input().backward();
                boolean left = input.input().left();
                boolean right = input.input().right();
                boolean jump = input.input().jump();
                boolean sneak = input.input().sneak();
                boolean sprint = input.input().sprint();
                float fwd = forward ? 1f : (backward ? -1f : 0f);
                float side = left ? 1f : (right ? -1f : 0f);
                if (jump == lastJump && sneak == lastSneak && sprint == lastSprint
                    && fwd == lastForwardInput && side == lastSidewaysInput) {
                    event.cancel();
                } else {
                    lastJump = jump;
                    lastSneak = sneak;
                    lastSprint = sprint;
                    lastForwardInput = fwd;
                    lastSidewaysInput = side;
                }
            }
        } else if (event.packet instanceof UpdatePlayerAbilitiesC2SPacket abilities) {
            if (enableFly.get() && !serverCanFly && abilities.isFlying()) {
                event.cancel();
            }
        } else if (event.packet instanceof TeleportConfirmC2SPacket) {
            exempt = true;
        } else if (event.packet instanceof PlayerMoveC2SPacket move) {
            handleRotationPacket(event, move);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerAbilitiesS2CPacket abilities) {
            serverCanFly = abilities.allowFlying();
        } else if (event.packet instanceof ChunkLoadDistanceS2CPacket packet) {
            if (enableViewDistance.get() && packet.getDistance() > 32) {
                event.packet = new ChunkLoadDistanceS2CPacket(32);
            }
        }
    }

    private void handleRotationPacket(PacketEvent.Send event, PlayerMoveC2SPacket packet) {
        float newPitch = packet.getPitch(serverPitch);
        float newYaw = packet.getYaw(serverYaw);

        // 特殊来源豁免
        if (exemptDupRot.get() && packet instanceof PlayerMoveC2SPacket.Full) {
            exempt = true;
        }
        if (exempt) {
            serverPitch = newPitch;
            serverYaw = newYaw;
            exempt = false;
            return;
        }

        if (newPitch == serverPitch && newYaw == serverYaw) {
            if (packet.changesLook() && enableRot.get()) {
                if (enableFullRot.get() && packet instanceof PlayerMoveC2SPacket.Full full) {
                    event.packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                        full.getX(mc.player.getX()),
                        full.getY(mc.player.getY()),
                        full.getZ(mc.player.getZ()),
                        full.isOnGround(),
                        full.horizontalCollision()
                    );
                } else if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround look) {
                    event.packet = new PlayerMoveC2SPacket.OnGroundOnly(
                        look.isOnGround(),
                        look.horizontalCollision()
                    );
                }
            }
        } else {
            serverPitch = newPitch;
            serverYaw = newYaw;
        }
    }

    @Override
    public String getInfoString() {
        return serverCanFly ? "可飞行" : "正常";
    }
}
