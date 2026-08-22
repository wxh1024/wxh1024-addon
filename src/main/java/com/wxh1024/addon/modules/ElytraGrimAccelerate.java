package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

/**
 * Grim 服务器鞘翅加速（从 SlimefunHelper ElytraGrimAccelerate 重写）
 * 取消服务器的减速速度包，并通过发送"预期高位位置包"欺骗 Grim 的速度校验
 */
public class ElytraGrimAccelerate extends Module {
    public static ElytraGrimAccelerate INSTANCE;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("模式")
        .description("模拟包构造方式")
        .defaultValue(Mode.SIMULATION)
        .build()
    );

    public final Setting<Double> maxVelocityAccept = sgGeneral.add(new DoubleSetting.Builder()
        .name("最大速度")
        .description("达到该速度后停止加速")
        .defaultValue(6.0)
        .min(0)
        .max(100)
        .build()
    );

    public final Setting<Double> minVelocityAccept = sgGeneral.add(new DoubleSetting.Builder()
        .name("最小速度")
        .description("低于该速度时开始加速")
        .defaultValue(3.6)
        .min(0)
        .max(100)
        .build()
    );

    public final Setting<Boolean> fixKickFromLag = sgGeneral.add(new BoolSetting.Builder()
        .name("修复卡顿踢出")
        .description("限制模拟包发送频率，避免因卡顿被服务器踢出")
        .defaultValue(true)
        .build()
    );

    public enum Mode {
        /** 在玩家当前位置上方模拟下落位置 */
        SIMULATION,
        /** 发送巨大坐标的异常包 */
        CRASH_PACKETS
    }

    private int lastSendMoveAndWaitSetBackTick;
    private int lastWorkingTick;
    private boolean currentTryWorking;
    private boolean currentWorking;
    /** 放行自己构造的移动包，避免被再次拦截 */
    private boolean suppressNext;

    public ElytraGrimAccelerate() {
        super(Wxh1024Addon.WXH1024, "Grim加速", "Grim 服务器下的鞘翅加速");
        INSTANCE = this;
    }

    @EventHandler
    private void onVelocityUpdate(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityVelocityUpdateS2CPacket packet)) return;
        if (mc.player == null || packet.getEntityId() != mc.player.getId()) return;

        if (mc.player.isGliding() && (isActive() || lastWorkingTick + 10 > mc.world.getTime())) {
            Vec3d velocity = packet.getVelocity();
            Vec3d playerVelocity = mc.player.getVelocity();
            if (velocity.horizontalLengthSquared() < 1.0E-2) {
                event.cancel();
            } else if (playerVelocity.horizontalLengthSquared() > 1.0E-2
                && playerVelocity.getHorizontal().dotProduct(velocity.getHorizontal()) < 0) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket)) return;
        if (suppressNext) {
            suppressNext = false;
            return;
        }
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        currentTryWorking = (isActive() || currentTryWorking)
            && player.isGliding()
            && !player.isOnGround()
            && !ElytraExtra.INSTANCE.canFireworkControlMotion();

        if (currentTryWorking) lastWorkingTick = (int) mc.world.getTime();

        if (!currentTryWorking) {
            currentWorking = false;
            return;
        }

        Vec3d velocity = player.getVelocity();
        double speed = velocity.length();
        if (currentWorking) {
            if (speed > maxVelocityAccept.get()) currentWorking = false;
        } else {
            if (speed < minVelocityAccept.get()) currentWorking = true;
        }

        if (currentWorking) {
            // 取消本 tick 的正常移动包，替换为模拟包
            event.cancel();
            if (fixKickFromLag.get()
                && mc.world.getTime() < lastSendMoveAndWaitSetBackTick + 20) {
                return;
            }
            PlayerMoveC2SPacket sim = createStorePacket(player);
            if (sim != null) {
                suppressNext = true;
                mc.getNetworkHandler().sendPacket(sim);
                lastSendMoveAndWaitSetBackTick = (int) mc.world.getTime();
            }
        }
    }

    private PlayerMoveC2SPacket createStorePacket(ClientPlayerEntity player) {
        double offsetY = 2.5 * ((mc.world.getTime() % 3) + 1);
        return switch (mode.get()) {
            case SIMULATION -> new PlayerMoveC2SPacket.Full(
                player.getX(),
                player.getY() + offsetY,
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.isOnGround(),
                player.horizontalCollision
            );
            case CRASH_PACKETS -> new PlayerMoveC2SPacket.Full(
                3.9999999E7D,
                player.getY() + offsetY,
                3.9999999E7D,
                player.getYaw(),
                player.getPitch(),
                true,
                player.horizontalCollision
            );
        };
    }

    @Override
    public String getInfoString() {
        return currentWorking ? "加速中" : "待命中";
    }
}
