package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<List<Block>> containers = sg.add(new BlockListSetting.Builder()
        .name("容器方块")
        .filter(block -> block.getDefaultState().hasBlockEntity())
        .defaultValue(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
            Blocks.SHULKER_BOX, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.HOPPER, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
            Blocks.ENDER_CHEST, Blocks.BREWING_STAND, Blocks.CRAFTER)
        .build());

    private final Setting<Integer> range = sg.add(new IntSetting.Builder()
        .name("位置宽容").description("附着方向穿透格数。0=仅附着方块，1~6=穿透。")
        .defaultValue(1).min(0).max(6).build());

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "Mixin 篡改 interactAt（旋转/放置入口）为开箱。");
    }

    /** 由 Mixin 的 interactAt 拦截点调用 */
    public void tryOpen(ItemFrameEntity frame) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Direction d = frame.getHorizontalFacing().getOpposite();
        BlockPos a = BlockPos.ofFloored(
            frame.getX() + d.getOffsetX() * 0.5,
            frame.getY() + d.getOffsetY() * 0.5,
            frame.getZ() + d.getOffsetZ() * 0.5);

        BlockPos c = null;
        for (int i = 0; i <= range.get(); i++) {
            BlockPos p = a.offset(d, i);
            if (containers.get().contains(mc.world.getBlockState(p).getBlock())) {
                c = p; break;
            }
        }
        if (c == null) return;

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
            new BlockHitResult(new Vec3d(c.getX() + 0.5, c.getY() + 0.5, c.getZ() + 0.5),
                Direction.UP, c, false), 0));
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
