package com.wxh1024.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chests = sgGeneral.add(new BoolSetting.Builder()
        .name("箱子")
        .description("开启箱子 / 陷阱箱")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> barrels = sgGeneral.add(new BoolSetting.Builder()
        .name("木桶")
        .description("开启木桶")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> shulkerBoxes = sgGeneral.add(new BoolSetting.Builder()
        .name("潜影盒")
        .description("开启潜影盒")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> dispensers = sgGeneral.add(new BoolSetting.Builder()
        .name("发射器")
        .description("开启发射器 / 投掷器")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> hoppers = sgGeneral.add(new BoolSetting.Builder()
        .name("漏斗")
        .description("开启漏斗")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> furnaces = sgGeneral.add(new BoolSetting.Builder()
        .name("熔炉")
        .description("开启熔炉 / 高炉 / 烟熏炉")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enderChests = sgGeneral.add(new BoolSetting.Builder()
        .name("末影箱")
        .description("开启末影箱")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> brewingStands = sgGeneral.add(new BoolSetting.Builder()
        .name("酿造台")
        .description("开启酿造台")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> crafters = sgGeneral.add(new BoolSetting.Builder()
        .name("合成器")
        .description("开启合成器")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> unconditionalIntercept = sgGeneral.add(new BoolSetting.Builder()
        .name("无条件拦截")
        .description("没找到容器时是否依然拦截展示框交互（不触发旋转物品等原版行为）。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("沿附着方向额外穿透距离。0=仅展示框直接附着的方块，1~6=穿透若干方块。")
        .defaultValue(1)
        .min(0)
        .max(6)
        .build()
    );

    public ItemFrameChestOpener() {
        super(
            Wxh1024Addon.WXH1024,
            "展示框开箱",
            "右键展示框打开展示框后方附着容器。支持普通/荧光展示框，位置宽容穿透，无条件拦截。"
        );
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        Entity entity = event.entity;
        // instanceof ItemFrameEntity 自动匹配普通展示框和荧光展示框 (GlowItemFrameEntity)
        if (!(entity instanceof ItemFrameEntity itemFrame)) return;

        // === 容器定位算法 ===
        // 展示框朝向 = 脱离墙面方向，附着方向 = 朝向的反方向（指向墙体/附着方块）
        Direction facing = itemFrame.getHorizontalFacing();
        Direction attachmentDir = facing.getOpposite();

        // 用精确坐标 + 半格偏移可靠计算附着方块位置，兼容墙壁/地面/天花板朝向
        Vec3d precisePos = itemFrame.getPos();
        BlockPos attachmentPos = BlockPos.ofFloored(
            precisePos.getX() + attachmentDir.getOffsetX() * 0.5,
            precisePos.getY() + attachmentDir.getOffsetY() * 0.5,
            precisePos.getZ() + attachmentDir.getOffsetZ() * 0.5
        );

        // 沿附着方向扫描容器（从附着方块开始，按位置宽容穿透）
        BlockPos containerPos = null;
        for (int dist = 0; dist <= rangeTolerance.get(); dist++) {
            BlockPos checkPos = attachmentPos.offset(attachmentDir, dist);
            if (isContainerEnabled(mc.world.getBlockEntity(checkPos))) {
                containerPos = checkPos;
                break;
            }
        }

        if (containerPos != null) {
            // GhostHand 风格开箱
            Vec3d hitPos = new Vec3d(
                containerPos.getX() + 0.5,
                containerPos.getY() + 0.5,
                containerPos.getZ() + 0.5
            );
            BlockHitResult containerHit = new BlockHitResult(
                hitPos, Direction.UP, containerPos, false
            );
            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, containerHit);
            if (result.isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            event.cancel();
        } else if (unconditionalIntercept.get()) {
            // 没找到容器但开了无条件拦截 → 拦截原版展示框交互
            event.cancel();
        }
        // 没找到容器且未开无条件拦截 → 走原版右键交互
    }

    private boolean isContainerEnabled(BlockEntity be) {
        if (be == null) return false;
        if (be instanceof ChestBlockEntity && chests.get()) return true;
        if (be instanceof BarrelBlockEntity && barrels.get()) return true;
        if (be instanceof ShulkerBoxBlockEntity && shulkerBoxes.get()) return true;
        if (be instanceof DispenserBlockEntity && dispensers.get()) return true;
        if (be instanceof HopperBlockEntity && hoppers.get()) return true;
        if (be instanceof FurnaceBlockEntity && furnaces.get()) return true;
        if (be instanceof EnderChestBlockEntity && enderChests.get()) return true;
        if (be instanceof BrewingStandBlockEntity && brewingStands.get()) return true;
        if (be instanceof CrafterBlockEntity && crafters.get()) return true;
        return false;
    }
}
