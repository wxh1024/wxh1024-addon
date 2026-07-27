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
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- 容器开关 ---
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

    // --- 定位 ---
    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("沿附着方向穿透扫描格数。0=仅附着方块，1~6=穿透若干方块。")
        .defaultValue(1)
        .min(0)
        .max(6)
        .build()
    );

    // --- 交互控制 ---
    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互")
        .description("阻止展示框原版交互（旋转物品、放置物品、挥手动画）。关闭时仅开箱不拦截。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容")
        .description("静默旋转视角使容器交互通过 Grim 等反作弊的光线追踪检测。")
        .defaultValue(false)
        .build()
    );

    public ItemFrameChestOpener() {
        super(
            Wxh1024Addon.WXH1024,
            "展示框开箱",
            "右键展示框打开展示框后方附着容器。支持反作弊兼容、位置宽容。"
        );
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        Entity entity = event.entity;
        if (!(entity instanceof ItemFrameEntity itemFrame)) return;

        // 彻底拦截：第一时间取消事件，阻止挥臂动画、交互包等任何原版侧漏
        if (interceptEnabled.get()) {
            event.cancel();
        }

        // === 定位附着方块 ===
        Direction facing = itemFrame.getHorizontalFacing();
        Direction attachmentDir = facing.getOpposite();
        BlockPos attachmentPos = BlockPos.ofFloored(
            itemFrame.getX() + attachmentDir.getOffsetX() * 0.5,
            itemFrame.getY() + attachmentDir.getOffsetY() * 0.5,
            itemFrame.getZ() + attachmentDir.getOffsetZ() * 0.5
        );

        // === 沿附着方向扫描，找到第一个容器就停止 ===
        BlockPos containerPos = null;
        int maxDist = rangeTolerance.get();
        for (int dist = 0; dist <= maxDist; dist++) {
            BlockPos checkPos = attachmentPos.offset(attachmentDir, dist);
            if (isContainerEnabled(mc.world.getBlockEntity(checkPos))) {
                containerPos = checkPos;
                break; // 找到即停止
            }
        }

        if (containerPos == null) return;

        Vec3d hitPos = new Vec3d(
            containerPos.getX() + 0.5,
            containerPos.getY() + 0.5,
            containerPos.getZ() + 0.5
        );
        BlockHitResult containerHit = new BlockHitResult(
            hitPos, Direction.UP, containerPos, false
        );

        if (antiCheatCompat.get()) {
            // === 发包开箱，Grim 绕过 ===
            if (canSee(containerPos, itemFrame)) {
                // 无需转头：容器在视线内，直接发包开箱
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND, containerHit, 0
                ));
                mc.player.swingHand(Hand.MAIN_HAND);
            } else {
                // 需要发包静默转头绕过 Grim 光线追踪
                float[] rots = calcRotations(hitPos);
                float prevYaw = mc.player.getYaw();
                float prevPitch = mc.player.getPitch();

                mc.player.setYaw(rots[0]);
                mc.player.setPitch(rots[1]);

                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    rots[0], rots[1], mc.player.isOnGround(), mc.player.horizontalCollision
                ));

                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(
                    Hand.MAIN_HAND, containerHit, 0
                ));
                mc.player.swingHand(Hand.MAIN_HAND);

                mc.player.setYaw(prevYaw);
                mc.player.setPitch(prevPitch);
            }
        } else {
            // === 标准模式 ===
            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, containerHit);
            if (result.isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private float[] calcRotations(Vec3d target) {
        Vec3d eye = mc.player.getEyePos();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        return new float[]{
            (float) Math.toDegrees(Math.atan2(-dx, dz)),
            (float) -Math.toDegrees(Math.atan2(dy, hDist))
        };
    }

    /** 检测玩家当前准星是否指向目标容器（兼容准星在展示框实体上的情况） */
    private boolean canSee(BlockPos containerPos, ItemFrameEntity itemFrame) {
        if (mc.crosshairTarget instanceof BlockHitResult hit) {
            return hit.getBlockPos().equals(containerPos);
        }
        // 准星在展示框实体上：检查展示框附着方块是否就是目标容器
        Direction facing = itemFrame.getHorizontalFacing();
        BlockPos attachedTo = itemFrame.getBlockPos().offset(facing.getOpposite());
        return attachedTo.equals(containerPos);
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
