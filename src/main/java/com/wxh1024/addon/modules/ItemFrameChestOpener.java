package com.wxh1024.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
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
        .description("开启后，右键展示框不再触发原版交互（旋转物品等），始终尝试开箱。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("额外穿透距离，0=仅紧贴的容器，1~6=中间隔若干方块也能开箱。")
        .defaultValue(1)
        .min(0)
        .max(6)
        .build()
    );

    public ItemFrameChestOpener() {
        super(
            Wxh1024Addon.WXH1024,
            "展示框开箱",
            "右键/左键展示框打开展示框后方附着容器。支持普通展示框和荧光展示框，位置宽容穿透，无条件拦截。"
        );
    }

    /** 右键交互：处理所有展示框类型（ItemFrameEntity / GlowItemFrameEntity） */
    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        Entity entity = event.entity;
        if (!(entity instanceof ItemFrameEntity itemFrame)) return;

        tryOpenContainer(itemFrame, event);
    }

    /** 左键攻击：同样重定向为开箱（可选由无条件拦截控制） */
    @EventHandler
    private void onAttackEntity(AttackEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        Entity entity = event.entity;
        if (!(entity instanceof ItemFrameEntity itemFrame)) return;

        tryOpenContainer(itemFrame, event);
    }

    /**
     * 核心逻辑：根据展示框附着方向扫描容器并尝试打开
     * @param itemFrame 展示框实体（ItemFrameEntity 或 GlowItemFrameEntity）
     * @param event     可取消的事件（InteractEntityEvent 或 AttackEntityEvent）
     */
    private void tryOpenContainer(ItemFrameEntity itemFrame, Object event) {
        // 根据展示框朝向确定附着方向，沿该方向扫描容器
        Direction facing = itemFrame.getHorizontalFacing();
        Direction attachmentDir = facing.getOpposite();
        BlockPos framePos = itemFrame.getBlockPos();

        // 扫描容器：从展示框位置沿附着方向搜索（距离 0 到 1+宽容距离）
        BlockPos containerPos = null;
        int maxDistance = 1 + rangeTolerance.get();
        for (int dist = 0; dist <= maxDistance; dist++) {
            BlockPos checkPos = framePos.offset(attachmentDir, dist);
            if (isContainerEnabled(mc.world.getBlockEntity(checkPos))) {
                containerPos = checkPos;
                break;
            }
        }

        if (containerPos != null) {
            // 构造 BlockHitResult，模仿 GhostHand 开箱
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
            cancelEvent(event);
        } else if (unconditionalIntercept.get()) {
            // 没找到容器但设置了无条件拦截，阻止原版展示框交互
            cancelEvent(event);
        }
        // 没找到容器且未开无条件拦截 → 不拦截，走原版交互
    }

    /** 统一取消事件，兼容 InteractEntityEvent 和 AttackEntityEvent */
    private void cancelEvent(Object event) {
        if (event instanceof InteractEntityEvent e) {
            e.cancel();
        } else if (event instanceof AttackEntityEvent e) {
            e.cancel();
        }
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
