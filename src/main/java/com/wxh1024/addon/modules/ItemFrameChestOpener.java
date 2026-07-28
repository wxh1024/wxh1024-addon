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

import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ItemFrameChestOpener extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

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

    private final Setting<Integer> rangeTolerance = sgGeneral.add(new IntSetting.Builder()
        .name("位置宽容")
        .description("附着方向穿透格数。0=仅附着方块，1~6=穿透。")
        .defaultValue(1).min(0).max(6).build());

    private final Setting<Boolean> interceptEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("拦截交互")
        .description("彗星 event.cancel() 拦截。阻止展示框原版交互包和挥手包。")
        .defaultValue(true).build());

    private final Setting<Boolean> antiCheatCompat = sgGeneral.add(new BoolSetting.Builder()
        .name("反作弊兼容")
        .description("直接发包开箱，跳过客户端校验。")
        .defaultValue(false).build());

    private final Setting<Boolean> debugMode = sgGeneral.add(new BoolSetting.Builder()
        .name("调试模式")
        .description("持续监测展示框相关数据包，关闭时自动复制日志到剪贴板。")
        .defaultValue(false).build());

    // ─── 调试状态 ───
    private final StringBuilder debugBuffer = new StringBuilder();
    private boolean wasDebugOn = false;

    public ItemFrameChestOpener() {
        super(Wxh1024Addon.WXH1024, "展示框开箱",
            "右键展示框打开展示框后方附着容器。");
    }

    // ─── 展示框交互 ───
    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (mc.world == null || mc.player == null || mc.interactionManager == null) return;
        if (!(event.entity instanceof ItemFrameEntity frame)) return;
        if (event.hand != Hand.MAIN_HAND) return; // 只处理主手，过滤副手重复事件

        checkDebugToggle();
        long t0 = debugOn() ? System.nanoTime() : 0;
        long ts = debugOn() ? System.currentTimeMillis() : 0;

        // 彗星 @At(HEAD) cancel
        if (interceptEnabled.get()) {
            event.cancel();
            debugLog("[拦截] event.cancel() +%.2fms ts=%d",
                (System.nanoTime() - t0) / 1e6, ts);
        }

        // 扫描
        Direction dir = frame.getHorizontalFacing().getOpposite();
        BlockPos attach = BlockPos.ofFloored(
            frame.getX() + dir.getOffsetX() * 0.5,
            frame.getY() + dir.getOffsetY() * 0.5,
            frame.getZ() + dir.getOffsetZ() * 0.5);
        debugLog("[定位] frame=%s dir=%s attach=%s tol=%d",
            frame.getBlockPos().toShortString(), dir, attach.toShortString(), rangeTolerance.get());

        BlockPos container = null;
        for (int d = 0; d <= rangeTolerance.get(); d++) {
            BlockPos check = attach.offset(dir, d);
            Block block = mc.world.getBlockState(check).getBlock();
            if (containers.get().contains(block)) {
                container = check;
                debugLog("[扫描命中] d=%d pos=%s block=%s +%.2fms",
                    d, check.toShortString(), block.getTranslationKey(),
                    (System.nanoTime() - t0) / 1e6);
                break;
            }
        }
        if (container == null) {
            debugLog("[未命中] 扫描%d格 +%.2fms",
                rangeTolerance.get(), (System.nanoTime() - t0) / 1e6);
            return;
        }

        // 开箱
        Vec3d pos = new Vec3d(container.getX() + 0.5, container.getY() + 0.5, container.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, container, false);

        if (antiCheatCompat.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            debugLog("[发包开箱] pos=%s +%.2fms",
                container.toShortString(), (System.nanoTime() - t0) / 1e6);
        } else {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            debugLog("[interactBlock开箱] pos=%s +%.2fms",
                container.toShortString(), (System.nanoTime() - t0) / 1e6);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // ─── 发包拦截：兜底拦截 + 调试监测 ───
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        checkDebugToggle();
        boolean interceptOn = interceptEnabled.get();
        boolean debug = debugOn();
        if (!interceptOn && !debug) return;

        // 展示框交互包：始终拦截 + 调试记录
        if (event.packet instanceof PlayerInteractEntityC2SPacket p) {
            try {
                int id = (int) p.getClass().getDeclaredField("entityId").get(p);
                if (mc.world != null && mc.world.getEntityById(id) instanceof ItemFrameEntity) {
                    if (interceptOn) event.cancel();
                    if (debug) debugLog("[发包拦截] PlayerInteractEntityC2SPacket entityId=%d %s",
                        id, interceptOn ? "已拦截" : "未拦截(开关关闭)");
                }
            } catch (Exception ignored) {}
            return;
        }

        // 方块交互包（开箱）：仅调试记录，不拦截
        if (debug && event.packet instanceof PlayerInteractBlockC2SPacket p) {
            debugLog("[发包] PlayerInteractBlockC2SPacket pos=%s",
                p.getBlockHitResult().getBlockPos().toShortString());
        }
        if (debug && event.packet instanceof HandSwingC2SPacket) {
            debugLog("[发包] HandSwingC2SPacket (开箱挥手)");
        }
    }

    // ─── 调试辅助 ───
    private boolean debugOn() { return debugMode.get(); }

    private void debugLog(String fmt, Object... args) {
        if (!debugOn()) return;
        String msg = String.format(fmt, args);
        info(msg);
        debugBuffer.append(msg).append('\n');
    }

    private void checkDebugToggle() {
        boolean on = debugOn();
        if (wasDebugOn && !on) {
            // 调试关闭 → 复制到剪贴板
            if (debugBuffer.length() > 0) {
                GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), debugBuffer.toString());
                info(String.format("调试日志已复制到剪贴板 (%d 行)", countLines()));
            }
            debugBuffer.setLength(0);
        }
        wasDebugOn = on;
    }

    private int countLines() {
        int n = 0;
        for (int i = 0; i < debugBuffer.length(); i++)
            if (debugBuffer.charAt(i) == '\n') n++;
        return n;
    }
}
