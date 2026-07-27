package com.wxh1024.addon.modules;

import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
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
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
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
        .name("箱子").description("箱子 / 陷阱箱").defaultValue(true).build());
    private final Setting<Boolean> barrels = sgGeneral.add(new BoolSetting.Builder()
        .name("木桶").description("木桶").defaultValue(true).build());
    private final Setting<Boolean> shulkerBoxes = sgGeneral.add(new BoolSetting.Builder()
        .name("潜影盒").description("潜影盒").defaultValue(true).build());
    private final Setting<Boolean> dispensers = sgGeneral.add(new BoolSetting.Builder()
        .name("发射器").description("发射器 / 投掷器").defaultValue(true).build());
    private final Setting<Boolean> hoppers = sgGeneral.add(new BoolSetting.Builder()
        .name("漏斗").description("漏斗").defaultValue(true).build());
    private final Setting<Boolean> furnaces = sgGeneral.add(new BoolSetting.Builder()
        .name("熔炉").description("熔炉 / 高炉 / 烟熏炉").defaultValue(true).build());
    private final Setting<Boolean> enderChests = sgGeneral.add(new BoolSetting.Builder()
        .name("末影箱").description("末影箱").defaultValue(true).build());
    private final Setting<Boolean> brewingStands = sgGeneral.add(new BoolSetting.Builder()
        .name("酿造台").description("酿造台").defaultValue(true).build());
    private final Setting<Boolean> crafters = sgGeneral.add(new BoolSetting.Builder()
        .name("合成器").description("合成器").defaultValue(true).build());

    // --- 定位 ---
    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容").description("沿附着方向穿透扫描格数。0=仅附着方块，1~6=穿透若干方块。")
        .defaultValue(1).min(0).max(6).build());

    // --- 交互 ---
    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互").description("阻止展示框原版交互（旋转物品、放置物品）。").defaultValue(true).build());
    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容").description("改为发包方式开箱，绕过客户端限制。").defaultValue(false).build());

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "右键展示框打开展示框后方附着容器。支持位置宽容扫描、反作弊发包绕过。");
    }

    // === 拦截：网络层拦截 PlayerInteractEntityC2SPacket ===
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!interceptEnabled.get()) return;
        if (!(event.packet instanceof PlayerInteractEntityC2SPacket p)) return;

        // 反射获取 entityId 并检查是否为展示框
        try {
            int id = (int) p.getClass().getDeclaredField("entityId").get(p);
            if (mc.world != null && mc.world.getEntityById(id) instanceof ItemFrameEntity) {
                event.cancel();
            }
        } catch (Exception ignored) {}
    }

    // === 开箱：InteractEntityEvent 仅负责扫描 + 开箱 ===
    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        if (!(event.entity instanceof ItemFrameEntity frame)) return;

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
