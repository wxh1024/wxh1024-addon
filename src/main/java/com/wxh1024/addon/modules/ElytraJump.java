package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.PlayerInput;

/**
 * 鞘翅跃起（从 SlimefunHelper ElytraJump 重写）
 * 地面自动跳跃疾跑起步，离地后自动打开鞘翅并持续前冲
 */
public class ElytraJump extends Module {
    public static ElytraJump INSTANCE;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> pitch = sgGeneral.add(new DoubleSetting.Builder()
        .name("俯仰角")
        .description("滑翔时设置的俯仰角")
        .defaultValue(80.0)
        .min(-90)
        .max(90)
        .build()
    );

    private final Setting<Boolean> sneak = sgGeneral.add(new BoolSetting.Builder()
        .name("潜行")
        .description("滑翔时是否按住潜行")
        .defaultValue(false)
        .build()
    );

    private boolean lastFallFly;

    public ElytraJump() {
        super(Wxh1024Addon.WXH1024, "ElytraJump", "自动起跳并打开鞘翅滑翔");
        INSTANCE = this;
    }

    private void applyInput(ClientPlayerEntity player, boolean jumping) {
        player.input.playerInput = new PlayerInput(true, false, false, false, jumping, sneak.get(), true);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        // 飞行或上一 tick 飞行时保持指定俯仰角
        if (player.isGliding() || lastFallFly) {
            player.setPitch((float) (double) pitch.get());
        }

        if (player.isOnGround()) {
            applyInput(player, true);
            player.setSprinting(true);
        } else {
            if (!player.isGliding()) {
                if (player.checkGliding() && mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
            applyInput(player, false);
        }

        lastFallFly = player.isGliding();
    }

    @Override
    public void onDeactivate() {
        lastFallFly = false;
    }
}
