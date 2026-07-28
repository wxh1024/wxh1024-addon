package com.wxh1024.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> containers = sgGeneral.add(new BlockListSetting.Builder()
        .name("容器方块")
        .defaultValue(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
            Blocks.SHULKER_BOX, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.HOPPER, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.ENDER_CHEST, Blocks.BREWING_STAND, Blocks.CRAFTER
        ).build());

    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("附着方向穿透格数。0=仅附着方块，1~6=穿透。")
        .defaultValue(1).min(0).max(6).build());

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "右键展示框打开展示框后方附着容器。");
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        if (!(event.entity instanceof ItemFrameEntity frame)) return;
        if (event.hand != Hand.MAIN_HAND) return;

        // 定位附着方块
        Direction dir = frame.getHorizontalFacing().getOpposite();
        BlockPos attach = BlockPos.ofFloored(
            frame.getX() + dir.getOffsetX() * 0.5,
            frame.getY() + dir.getOffsetY() * 0.5,
            frame.getZ() + dir.getOffsetZ() * 0.5);

        // 扫描容器
        BlockPos container = null;
        for (int d = 0; d <= rangeTolerance.get(); d++) {
            if (containers.get().contains(mc.world.getBlockState(attach.offset(dir, d)).getBlock())) {
                container = attach.offset(dir, d);
                break;
            }
        }
        if (container == null) return;

        // 开箱
        Vec3d pos = new Vec3d(container.getX() + 0.5, container.getY() + 0.5, container.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, container, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
