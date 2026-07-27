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
import net.minecraft.client.network.PlayerListEntry;
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
        .description("沿附着方向额外穿透格数。0=仅附着方块，1~6=穿透若干方块。")
        .defaultValue(1)
        .min(0)
        .max(6)
        .build()
    );

    // --- 拦截控制 ---
    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互")
        .description("总开关。关闭后本模块完全不影响展示框。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容")
        .description("适配 Grim 等反作弊：不取消展示框事件，改为双包并行使交互不被标记异常。")
        .defaultValue(false)
        .build()
    );

    // --- 防重复 ---
    private final Setting<Boolean> dynamicCooldown = sgGeneral.add(new BoolSetting.Builder()
        .name("动态延迟")
        .description("防重复冷却根据网络延迟（ping）自动调整。关闭则使用下方自定义值。")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> customCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("自定义延迟")
        .description("手动设置防重复冷却时间（毫秒）。0=不限制。")
        .defaultValue(300)
        .min(0)
        .max(5000)
        .visible(() -> !dynamicCooldown.get())
        .build()
    );

    private long lastOpenTime = 0;

    public ItemFrameChestOpener() {
        super(
            Wxh1024Addon.WXH1024,
            "展示框开箱",
            "右键展示框打开展示框后方附着容器。支持反作弊兼容、动态延迟、位置宽容。"
        );
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;

        // 总开关：不拦截时直接放行
        if (!interceptEnabled.get()) return;

        Entity entity = event.entity;
        // instanceof ItemFrameEntity 自动匹配普通展示框和荧光展示框 (GlowItemFrameEntity)
        if (!(entity instanceof ItemFrameEntity itemFrame)) return;

        // === 容器定位 ===
        Direction facing = itemFrame.getHorizontalFacing();
        Direction attachmentDir = facing.getOpposite();
        BlockPos attachmentPos = BlockPos.ofFloored(
            itemFrame.getX() + attachmentDir.getOffsetX() * 0.5,
            itemFrame.getY() + attachmentDir.getOffsetY() * 0.5,
            itemFrame.getZ() + attachmentDir.getOffsetZ() * 0.5
        );

        // 扫描容器
        BlockPos containerPos = null;
        int maxDist = rangeTolerance.get();
        for (int dist = 0; dist <= maxDist; dist++) {
            BlockPos checkPos = attachmentPos.offset(attachmentDir, dist);
            if (isContainerEnabled(mc.world.getBlockEntity(checkPos))) {
                containerPos = checkPos;
                break;
            }
        }

        if (containerPos != null) {
            // --- 找到容器：开箱 ---
            boolean grim = antiCheatCompat.get();

            // 防重复冷却
            if (dynamicCooldown.get() || customCooldown.get() > 0) {
                int cooldownMs = dynamicCooldown.get() ? Math.max(getPing() * 2, 100) : customCooldown.get();
                long now = System.currentTimeMillis();
                if (now - lastOpenTime < cooldownMs) {
                    if (!grim) event.cancel(); // 标准模式：拦截但不重复开箱
                    return;
                }
                lastOpenTime = now;
            }

            // 标准模式：提前取消事件
            if (!grim) event.cancel();

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
        } else {
            // --- 未找到容器：无条件拦截（固定行为） ---
            if (!antiCheatCompat.get()) {
                event.cancel();
            }
            // Grim 兼容模式下不拦截，走原版展示框交互
        }
    }

    /** 获取当前网络延迟（毫秒），用于动态冷却计算 */
    private int getPing() {
        if (mc.player == null || mc.getNetworkHandler() == null) return 300;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return entry != null ? entry.getLatency() : 300;
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
