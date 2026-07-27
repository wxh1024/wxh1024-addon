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
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chests = sgGeneral.add(new BoolSetting.Builder()
        .name("箱子").defaultValue(true).build());
    private final Setting<Boolean> barrels = sgGeneral.add(new BoolSetting.Builder()
        .name("木桶").defaultValue(true).build());
    private final Setting<Boolean> shulkerBoxes = sgGeneral.add(new BoolSetting.Builder()
        .name("潜影盒").defaultValue(true).build());
    private final Setting<Boolean> dispensers = sgGeneral.add(new BoolSetting.Builder()
        .name("发射器").defaultValue(true).build());
    private final Setting<Boolean> hoppers = sgGeneral.add(new BoolSetting.Builder()
        .name("漏斗").defaultValue(true).build());
    private final Setting<Boolean> furnaces = sgGeneral.add(new BoolSetting.Builder()
        .name("熔炉").defaultValue(true).build());
    private final Setting<Boolean> enderChests = sgGeneral.add(new BoolSetting.Builder()
        .name("末影箱").defaultValue(true).build());
    private final Setting<Boolean> brewingStands = sgGeneral.add(new BoolSetting.Builder()
        .name("酿造台").defaultValue(true).build());
    private final Setting<Boolean> crafters = sgGeneral.add(new BoolSetting.Builder()
        .name("合成器").defaultValue(true).build());

    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容").description("附着方向穿透格数。0=仅附着方块，1~6=穿透。")
        .defaultValue(1).min(0).max(6).build());

    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互").description("取消展示框交互事件。彗星 @At(HEAD) 注入，cancel 阻止整个原版方法。")
        .defaultValue(true).build());

    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容").description("发包方式开箱，绕过客户端限制。").defaultValue(false).build());

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "右键展示框打开展示框后方附着容器。");
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        if (!(event.entity instanceof ItemFrameEntity frame)) return;

        // Meteor @At(HEAD) 注入 → cancel 阻止整个 interactEntity() 方法
        if (interceptEnabled.get()) event.cancel();

        // 扫描容器
        Direction dir = frame.getHorizontalFacing().getOpposite();
        BlockPos attach = BlockPos.ofFloored(
            frame.getX() + dir.getOffsetX() * 0.5,
            frame.getY() + dir.getOffsetY() * 0.5,
            frame.getZ() + dir.getOffsetZ() * 0.5);

        BlockPos container = null;
        for (int d = 0; d <= rangeTolerance.get(); d++) {
            if (isContainerEnabled(mc.world.getBlockEntity(attach.offset(dir, d)))) {
                container = attach.offset(dir, d);
                break;
            }
        }
        if (container == null) return;
        if (!isContainerEnabled(mc.world.getBlockEntity(container))) return;

        // 开箱
        Vec3d pos = new Vec3d(container.getX() + 0.5, container.getY() + 0.5, container.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, container, false);

        if (antiCheatCompat.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
        } else {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
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
