package com.wxh1024.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- 容器选择（彗星方块选择器）---
    private final Setting<List<Block>> containers = sgGeneral.add(new BlockListSetting.Builder()
        .name("容器方块")
        .description("要开启的容器类型（彗星方块选择器）。")
        .defaultValue(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
            Blocks.SHULKER_BOX, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.HOPPER, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.ENDER_CHEST, Blocks.BREWING_STAND, Blocks.CRAFTER
        )
        .build()
    );

    // --- 定位 ---
    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("附着方向穿透格数。0=仅附着方块，1~6=穿透。")
        .defaultValue(1).min(0).max(6).build());

    // --- 拦截 ---
    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互")
        .description("彻底阻止展示框原版交互。事件层 + 发包层双保险。")
        .defaultValue(true).build());

    // --- 开箱方式 ---
    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容")
        .description("直接发包开箱，跳过客户端校验。")
        .defaultValue(false).build());

    /** 标记：需要拦截下一个挥手臂包（展示框交互的挥手） */
    private boolean blockNextSwing = false;

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "右键展示框打开展示框后方附着容器。彗星方块选择器配置容器类型。");
    }

    // ─── 第一层拦截：事件层（@At(HEAD) 注入，最早时机） ───
    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        if (!(event.entity instanceof ItemFrameEntity frame)) return;

        // 事件层 cancel → mixin 调用 CallbackInfo.cancel() → 整个原版方法返回
        // 标记拦截下一个挥手臂包（来自展示框交互的挥手）
        if (interceptEnabled.get()) {
            event.cancel();
            blockNextSwing = true;
        }

        // 扫描 + 开箱
        Direction dir = frame.getHorizontalFacing().getOpposite();
        BlockPos attach = BlockPos.ofFloored(
            frame.getX() + dir.getOffsetX() * 0.5,
            frame.getY() + dir.getOffsetY() * 0.5,
            frame.getZ() + dir.getOffsetZ() * 0.5);

        BlockPos container = null;
        for (int d = 0; d <= rangeTolerance.get(); d++) {
            if (containers.get().contains(mc.world.getBlockState(attach.offset(dir, d)).getBlock())) {
                container = attach.offset(dir, d);
                break;
            }
        }
        if (container == null) return;

        Vec3d pos = new Vec3d(container.getX() + 0.5, container.getY() + 0.5, container.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, container, false);

        if (antiCheatCompat.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        } else {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ─── 第二层拦截：发包层（兜底拦截交互包 + 挥手包） ───
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!interceptEnabled.get()) return;

        // 拦截展示框挥手包
        if (event.packet instanceof HandSwingC2SPacket) {
            if (blockNextSwing) {
                event.cancel();
                blockNextSwing = false;
            }
            return;
        }

        // 拦截展示框交互包
        if (event.packet instanceof PlayerInteractEntityC2SPacket p) {
            try {
                int id = (int) p.getClass().getDeclaredField("entityId").get(p);
                if (mc.world != null && mc.world.getEntityById(id) instanceof ItemFrameEntity) {
                    event.cancel();
                }
            } catch (Exception ignored) {}
        }
    }
}
