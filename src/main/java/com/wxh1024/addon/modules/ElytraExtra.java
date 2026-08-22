package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import com.wxh1024.addon.utils.elytra.ElytraShared;
import com.wxh1024.addon.utils.elytra.EntityUtils;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 高级鞘翅飞行控制（从 SlimefunHelper ElytraExtra 重写）
 * 胸甲飞行 / 自动换装 / 烟花管理 / 自动起飞 / no-kinetic / auto-rescale / 落地免伤
 */
public class ElytraExtra extends Module {
    public static ElytraExtra INSTANCE;

    private final SettingGroup sgGeneral = settings.createGroup("常规");
    private final SettingGroup sgArmor = settings.createGroup("胸甲飞行");
    private final SettingGroup sgFirework = settings.createGroup("烟花");
    private final SettingGroup sgDetect = settings.createGroup("反检测");
    private final SettingGroup sgLanding = settings.createGroup("落地");

    // ===== 常规 =====
    public final Setting<Boolean> autoTakeOff = sgGeneral.add(new BoolSetting.Builder().name("自动起飞").description("落地自动跳跃，离地自动打开鞘翅").defaultValue(false).build());
    public final Setting<Boolean> autoTakeOffFireworksFromGround = sgGeneral.add(new BoolSetting.Builder().name("地面起飞放烟花").description("从地面自动起飞时立即放置烟花").defaultValue(false).build());
    public final Setting<Boolean> flyRocketOnFirstOff = sgGeneral.add(new BoolSetting.Builder().name("首次起飞放烟花").description("每次起飞时立即放置烟花").defaultValue(false).build());
    public final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder().name("自动换鞘翅").description("飞行时自动装备鞘翅，落地后换回").defaultValue(false).build());
    public final Setting<Boolean> elytraUnbreakable = sgGeneral.add(new BoolSetting.Builder().name("鞘翅无限耐久").description("周期性重新起飞避免耐久消耗（实验性）").defaultValue(false).build());

    // ===== 胸甲飞行 =====
    public final Setting<Boolean> armorFly = sgArmor.add(new BoolSetting.Builder().name("胸甲飞行").description("穿胸甲也能滑翔：飞行时自动借用鞘翅").defaultValue(false).build());
    public final Setting<ArmorFlyMode> armorMode = sgArmor.add(new EnumSetting.Builder<ArmorFlyMode>().name("换装模式").description("换装事务的触发时机").defaultValue(ArmorFlyMode.TICK).build());
    public final Setting<Boolean> enableOnGroundFly = sgArmor.add(new BoolSetting.Builder().name("贴地飞行").description("允许贴近地面飞行").defaultValue(false).build());
    public final Setting<Boolean> enableLiquidFly = sgArmor.add(new BoolSetting.Builder().name("水面飞行").description("允许贴水飞行").defaultValue(true).build());
    public final Setting<Boolean> enableLiquidFlyOnlyFireworks = sgArmor.add(new BoolSetting.Builder().name("水面飞行仅烟花").description("水面飞行时仅靠烟花推进").defaultValue(false).build());
    public final Setting<Boolean> landAutoClose = sgArmor.add(new BoolSetting.Builder().name("落地自动关闭").description("落地后自动关闭胸甲飞行").defaultValue(false).build());
    public final Setting<Boolean> landAutoSneak = sgArmor.add(new BoolSetting.Builder().name("落地自动潜行").description("落地后自动按住潜行").defaultValue(false).build());
    public final Setting<Boolean> poseFix = sgArmor.add(new BoolSetting.Builder().name("姿态修正").description("胸甲飞行时保持站立姿态").defaultValue(true).build());
    public final Setting<Boolean> forceArmor = sgArmor.add(new BoolSetting.Builder().name("强制借用鞘翅").description("手动起飞时也强制使用胸甲飞行事务").defaultValue(false).build());

    // ===== 烟花 =====
    public final Setting<Boolean> autoRocket = sgFirework.add(new BoolSetting.Builder().name("自动放烟花").description("烟花耗尽后自动使用下一发").defaultValue(true).build());
    public final Setting<Integer> autoRocketInterval = sgFirework.add(new IntSetting.Builder().name("放烟花间隔").description("两次自动放烟花的间隔 tick").defaultValue(20).min(10).max(200).build());
    public final Setting<Boolean> rocketBoost = sgFirework.add(new BoolSetting.Builder().name("烟花推进").description("飞行时沿视线方向持续加速").defaultValue(false).build());
    public final Setting<Double> rocketBoostSpeed = sgFirework.add(new DoubleSetting.Builder().name("推进速度").description("烟花推进的目标速度").defaultValue(1.8).min(0).max(10).build());
    public final Setting<Boolean> rocketBoostUseRescale = sgFirework.add(new BoolSetting.Builder().name("推进使用缩放").description("推进速度也应用自动缩放限制").defaultValue(false).build());
    public final Setting<Boolean> noEnoughFireworksNotify = sgFirework.add(new BoolSetting.Builder().name("无烟花提醒").description("找不到烟花时提醒").defaultValue(true).build());

    // ===== 反检测 =====
    public final Setting<Boolean> noKineticMode = sgDetect.add(new BoolSetting.Builder().name("无击退模式").description("绕过 Grim 的击退检测（速度异常时翻转视角）").defaultValue(false).build());
    public final Setting<Boolean> autoRescale = sgDetect.add(new BoolSetting.Builder().name("自动缩放").description("将速度限制在视线构造的盒内，规避 Grim 三轴检测").defaultValue(false).build());
    public final Setting<Al> autoRescaleAl = sgDetect.add(new EnumSetting.Builder<Al>().name("缩放算法").description("自动缩放算法版本").defaultValue(Al.V1).build());
    public final Setting<Double> autoRescaleAmount = sgDetect.add(new DoubleSetting.Builder().name("缩放阈值").description("盒限制的最大长度").defaultValue(1.0).min(0.01).max(10).build());
    public final Setting<Double> yClimbCap = sgDetect.add(new DoubleSetting.Builder().name("垂直爬升上限").description("垂直方向的最大速度").defaultValue(0.8).min(0).max(3).build());

    // ===== 落地 =====
    public final Setting<Boolean> noFallLanding = sgLanding.add(new BoolSetting.Builder().name("落地免伤").description("飞行落地时欺骗服务器避免摔落伤害").defaultValue(false).build());

    // ===== 运行时状态 =====
    public int thisFallFlyingIsArmorFly = -1;
    public int thisFallFlyingIsAutoSwitch = -1;
    private int thisTickSwitchingIndex = -1;
    private boolean thisTickHasStartFallFly;
    private boolean shouldFlushRocketsThisTick;
    private boolean autoTakeOffFlag;
    private boolean lastOnGroundOrInWaterAutoTakeOff;
    private int nextLandingSneak;
    private int lastFlushRocketTick;
    private FireworkRocketEntity lastFireworkRocket;
    private int lastFireworkRocketTick;
    private int lastFireworkThresholdTime;
    private int lastLaunchFindNoFireworks = -1;
    private final Deque<ItemStack> delayQueue = new ArrayDeque<>();
    private boolean flushing;
    private int triggerKinetic;
    private float lastPitch, lastYaw;
    private int glidingTicks;
    private int autoRocketTimer;
    private long nextFlyFireworksTick = -1;
    private int manuallySwitchTick = -1;

    public ElytraExtra() {
        super(Wxh1024Addon.WXH1024, "甲飞", "高级鞘翅飞行控制：胸甲飞行/烟花管理/反检测");
        INSTANCE = this;
    }

    public enum ArmorFlyMode { LAZY, TICK_LEGACY, TICK }
    public enum Al { V1, V2 }

    // ===== 公共接口（供 ElytraFlight 调用） =====

    public boolean canFireworkControlMotion() {
        return canFireworkControlMotion(3);
    }

    public boolean canFireworkControlMotion(int extraTicks) {
        if (lastFireworkRocket == null) return false;
        if (lastFireworkRocket.isAlive()) return true;
        return getTickSinceLastFirework() < extraTicks;
    }

    public int getTickSinceLastFirework() {
        if (lastFireworkRocket == null) return Integer.MAX_VALUE;
        return lastFireworkRocket.isAlive() ? 0 : (int) mc.world.getTime() - lastFireworkRocketTick - lastFireworkThresholdTime;
    }

    public boolean shouldApplyOnGroundFly() {
        return mc.player != null && mc.player.isGliding() && armorFly.get()
            && thisFallFlyingIsArmorFly != -1 && enableOnGroundFly.get();
    }

    public void autoTakeoff() {
        autoTakeOffFlag = true;
    }

    public Vec3d applyAxisLimit(Vec3d currentMotion, Vec3d currentRotation, boolean applyGravity) {
        if (!autoRescale.get()) return currentMotion;
        return autoRescaleAl.get() == Al.V1
            ? applyAxisLimit1(currentMotion, currentRotation)
            : applyAxisLimit2(currentMotion, currentRotation);
    }

    // ===== 事件 =====

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        lastPitch = player.getPitch();
        lastYaw = player.getYaw();
        ElytraShared.lastHasGroundSupport = player.isOnGround();
        ElytraShared.lastInWater = player.isTouchingWater();
        ElytraShared.lastInLava = player.isInLava();
        ElytraShared.lastKnownClientVelocity = player.getVelocity();
        glidingTicks = player.isGliding() ? glidingTicks + 1 : 0;

        if (!isActive()) return;

        // 胸甲飞行每 tick 逻辑
        if (armorFly.get() && player.isGliding()) {
            if (canContinueGliding()) {
                lastFlushRocketTick++;
                if (lastFlushRocketTick > 10 && canGlide(player.getEquippedStack(EquipmentSlot.CHEST))) {
                    shouldFlushRocketsThisTick = true;
                }
            }
            if (armorMode.get() != ArmorFlyMode.LAZY && thisTickSwitchingIndex == -1) {
                if (thisTickHasStartFallFly) {
                    thisTickHasStartFallFly = false;
                    if (onSwitchItemArmorFallFlying()) {
                        sendStartFallFlying();
                        flushRockets();
                        thisTickHasStartFallFly = true;
                    } else {
                        clearRockets();
                        player.stopGliding();
                        endArmorFlyTransaction(false);
                    }
                }
            }
        } else {
            endArmorFlyTransaction(false);
        }

        // 自动换鞘翅回退
        if (thisFallFlyingIsAutoSwitch != -1 && !player.isGliding()) {
            switchSlotToArmor(thisFallFlyingIsAutoSwitch);
            thisFallFlyingIsAutoSwitch = -1;
        }

        // 贴地飞行抬头归零
        if (shouldApplyOnGroundFly() && ElytraShared.lastHasGroundSupport && player.getPitch() > 0) {
            player.setPitch(0);
        }

        // 烟花推进
        if (player.isGliding() && rocketBoost.get() && canFireworkControlMotion(0)) {
            Vec3d rot = player.getRotationVector();
            Vec3d modified = rot.multiply(rocketBoostSpeed.get());
            if (rocketBoostUseRescale.get()) {
                player.setVelocity(applyAxisLimit(modified, rot, !player.hasNoGravity()));
                float yaw = player.getYaw();
                player.setYaw(mc.world.getTime() % 2 == 0 ? yaw + 0.01F : yaw - 0.01F);
            } else {
                player.setVelocity(modified);
            }
        }

        // no-kinetic：被击退时翻转视角
        if (noKineticMode.get() && thisFallFlyingIsArmorFly == -1 && player.isGliding()) {
            handleNoKinetic(player);
        }
    }

    private void handleNoKinetic(ClientPlayerEntity player) {
        Vec3d velocity = player.getVelocity();
        if (velocity.horizontalLength() > 0.3 && player.getRotationVector().dotProduct(velocity) < 0) {
            triggerKinetic = 2;
        }
        if (triggerKinetic > 0) {
            triggerKinetic--;
            if (canFireworkControlMotion()) {
                player.setPitch(triggerKinetic % 2 == 0 ? -90.001F : 89.999F);
            } else {
                player.setYaw(player.getYaw() + 180);
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractItemC2SPacket usePacket) {
            if (!flushing && (thisFallFlyingIsArmorFly != -1 || thisFallFlyingIsAutoSwitch != -1)) {
                ItemStack stack = mc.player.getStackInHand(usePacket.getHand());
                if (!stack.isEmpty() && canBeUsedAsFireworks(stack)) {
                    if (stack.isOf(Items.FIREWORK_ROCKET)) delayQueue.removeIf(s -> s.isEmpty() || s.isOf(Items.FIREWORK_ROCKET));
                    delayQueue.add(stack.copy());
                    event.cancel();
                }
            }
        } else if (event.packet instanceof ClientCommandC2SPacket command) {
            if (command.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING) {
                manuallySwitchTick = (int) mc.world.getTime();
                if (nextFlyFireworksTick == mc.world.getTime() && mc.player.isGliding()) {
                    shouldFlushRocketsThisTick = true;
                    delayQueue.add(ItemStack.EMPTY);
                    mc.player.setSprinting(false);
                }
            }
        } else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround move) {
            if (mc.player != null && noFallLanding.get() && mc.player.isGliding() && move.isOnGround()) {
                event.packet = new PlayerMoveC2SPacket.PositionAndOnGround(
                    move.getX(mc.player.getX()), move.getY(mc.player.getY()) + 9E-8, move.getZ(mc.player.getZ()),
                    false, move.horizontalCollision());
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            lastLagBackTick = (int) mc.world.getTime();
        }
    }

    private int lastLagBackTick;

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (event.entity instanceof FireworkRocketEntity firework
            && mc.player != null && mc.player.isGliding()
            && firework.getOwner() == mc.player) {
            lastFireworkRocket = firework;
            lastFireworkRocketTick = (int) mc.world.getTime();
            lastFireworkThresholdTime = 0;
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (event.entity == lastFireworkRocket) {
            lastFireworkThresholdTime = lastFireworkRocket.age;
            autoRocketTimer = 0;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        if (shouldFlushRocketsThisTick) {
            flushRockets();
            shouldFlushRocketsThisTick = false;
        }
        if (thisTickSwitchingIndex != -1) {
            switchSlotToArmor(thisTickSwitchingIndex);
            thisTickSwitchingIndex = -1;
        }
        if (manuallySwitchTick == mc.world.getTime() && armorFly.get() && forceArmor.get() && thisFallFlyingIsArmorFly == -1) {
            startArmorFlyTransaction(-1);
        }
        if (thisFallFlyingIsArmorFly != -1 && poseFix.get()) {
            player.setPose(EntityPose.STANDING);
        }

        handleAutoTakeOff(player);

        if (!player.isGliding() && nextLandingSneak > 0) {
            nextLandingSneak--;
            if (nextLandingSneak <= 1 && !player.isSneaking()) {
                player.input.playerInput = new PlayerInput(
                    player.input.playerInput.forward(), player.input.playerInput.backward(),
                    player.input.playerInput.left(), player.input.playerInput.right(),
                    player.input.playerInput.jump(), true, player.input.playerInput.sprint());
            }
        }
    }

    private void handleAutoTakeOff(ClientPlayerEntity player) {
        if (!autoTakeOffFlag) {
            lastOnGroundOrInWaterAutoTakeOff = false;
            return;
        }
        if (player.isGliding()) {
            if (glidingTicks > 3 && !player.isTouchingWater()) {
                lastOnGroundOrInWaterAutoTakeOff = false;
                autoTakeOffFlag = false;
            } else {
                lastOnGroundOrInWaterAutoTakeOff = true;
            }
            return;
        }
        if (hasGlidingItem() && player.isOnGround()) {
            lastOnGroundOrInWaterAutoTakeOff = true;
            player.input.playerInput = new PlayerInput(true, false, false, false, true, player.input.playerInput.sneak(), true);
        } else if (hasGlidingItem() && player.checkGliding()) {
            sendStartFallFlying();
            if (autoTakeOffFireworksFromGround.get() && lastOnGroundOrInWaterAutoTakeOff) {
                nextFlyFireworksTick = mc.world.getTime();
            }
        } else if (!player.isTouchingWater()) {
            autoTakeOffFlag = false;
            lastOnGroundOrInWaterAutoTakeOff = false;
        } else {
            lastOnGroundOrInWaterAutoTakeOff = true;
        }
    }

    // ===== 胸甲飞行事务 =====

    private void startArmorFlyTransaction(int value) {
        if (thisFallFlyingIsArmorFly != -1) return;
        if (value != -1) {
            thisFallFlyingIsArmorFly = value;
            thisFallFlyingIsAutoSwitch = -1;
            switchSlotToArmor(value);
        } else if (hasGlidingEquipments()) {
            int idx = findElytra();
            if (idx != -1) {
                thisFallFlyingIsArmorFly = idx;
                thisFallFlyingIsAutoSwitch = -1;
                switchSlotToArmor(idx);
            }
        }
    }

    private void endArmorFlyTransaction(boolean continueFly) {
        if (thisFallFlyingIsArmorFly == -1) return;
        boolean willContinue = continueFly && canContinueGliding();
        if (willContinue) {
            switchSlotToArmor(thisFallFlyingIsArmorFly);
            sendStartFallFlying();
            flushRockets();
            autoTakeoff();
        } else {
            clearRockets();
        }
        if (mc.player.isOnGround() && !mc.player.isGliding() && armorFly.get() && landAutoClose.get()) {
            toggle();
        }
        if (landAutoSneak.get() && !mc.player.isSneaking()) {
            nextLandingSneak = 2;
        }
        if (autoSwitch.get() && mc.player.isGliding() && willContinue) {
            thisFallFlyingIsAutoSwitch = thisFallFlyingIsArmorFly;
        }
        thisFallFlyingIsArmorFly = -1;
    }

    private boolean onSwitchItemArmorFallFlying() {
        if (!canContinueArmorGliding()) return false;
        ItemStack stack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (!canGlide(stack)) {
            int idx = findElytra();
            if (idx != -1) {
                thisTickSwitchingIndex = idx;
                thisFallFlyingIsArmorFly = idx;
                lastFlushRocketTick = 0;
                thisTickHasStartFallFly = true;
                return true;
            }
            return false;
        }
        thisTickSwitchingIndex = -1;
        return true;
    }

    private void sendStartFallFlying() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
    }

    // ===== 装备查询/切换 =====

    private boolean canGlide(ItemStack stack) {
        return mc.player.canGlideWith(stack, EquipmentSlot.CHEST);
    }

    public boolean canContinueGliding() {
        return !mc.player.isTouchingWater()
            && !mc.player.getAbilities().flying
            && !mc.player.isOnGround()
            && !mc.player.hasVehicle()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    public boolean canContinueArmorGliding() {
        boolean liquidOk = enableLiquidFly.get()
            ? (!mc.player.isTouchingWater() || !enableLiquidFlyOnlyFireworks.get() || canFireworkControlMotion(0))
            : !mc.player.isTouchingWater();
        boolean groundOk = enableOnGroundFly.get()
            ? (!ElytraShared.lastHasGroundSupport || canFireworkControlMotion(0))
            : !mc.player.isOnGround();
        return liquidOk
            && !mc.player.getAbilities().flying
            && groundOk
            && !mc.player.hasVehicle()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    public boolean hasGlidingEquipments() {
        return canGlide(mc.player.getEquippedStack(EquipmentSlot.CHEST));
    }

    public boolean hasGlidingItem() {
        return findElytra() != -1;
    }

    private int findElytra() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) if (canGlide(inv.getStack(i))) return i;
        for (int i = 9; i < 36; i++) if (canGlide(inv.getStack(i))) return i;
        if (canGlide(mc.player.getOffHandStack())) return 40;
        return -1;
    }

    private void switchSlotToArmor(int targetSlot) {
        if (mc.interactionManager == null || mc.player == null) return;
        int chestSlot = 38;
        int target = targetSlot < 9 ? 36 + targetSlot : targetSlot;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, chestSlot, target, SlotActionType.SWAP, mc.player);
    }

    // ===== 烟花管理 =====

    public boolean canBeUsedAsFireworks(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(Items.FIREWORK_ROCKET);
    }

    private ItemStack findRocket() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) if (canBeUsedAsFireworks(inv.getStack(i))) return inv.getStack(i);
        if (canBeUsedAsFireworks(mc.player.getOffHandStack())) return mc.player.getOffHandStack();
        return null;
    }

    private int getRocketLevel(ItemStack stack) {
        if (stack.isOf(Items.FIREWORK_ROCKET)) {
            FireworksComponent component = stack.get(DataComponentTypes.FIREWORKS);
            if (component != null) return 1 + component.flightDuration();
        }
        return 1;
    }

    public void launchFirework(float pitch, float yaw) {
        ItemStack rocket = findRocket();
        if (rocket == null) {
            onNoFireworks();
            return;
        }
        int level = getRocketLevel(rocket);
        boolean auto = autoRocket.get() && lastFireworkRocket != null;
        boolean use = false;
        if (auto) {
            if (lastFireworkRocket.isAlive()) return;
            if (mc.world.getTime() > lastFireworkRocketTick + lastFireworkThresholdTime + 1) use = true;
        }
        if (!use && shouldLaunchNextFirework() && timerCanFire(level)) use = true;
        if (use) {
            fireTimer();
            sendCustomUseFireworkPacket(pitch, yaw);
        }
        onHasFirework();
    }

    public boolean shouldLaunchNextFirework() {
        if (lastFireworkRocket != null) {
            if (lastFireworkRocket.isAlive()) return false;
            return getTickSinceLastFirework() >= 1;
        }
        return true;
    }

    private boolean timerCanFire(int level) {
        if (autoRocketTimer == Integer.MAX_VALUE - 10) return true;
        return mc.world.getTime() - lastFireworkRocketTick - lastFireworkThresholdTime > autoRocketInterval.get() * (long) level;
    }

    private void fireTimer() {
        autoRocketTimer = 0;
    }

    private void onNoFireworks() {
        if (lastLaunchFindNoFireworks < mc.world.getTime() - 10 * 20L) {
            lastLaunchFindNoFireworks = (int) mc.world.getTime();
            if (noEnoughFireworksNotify.get()) info("物品栏中没有找到烟花");
        }
    }

    private void onHasFirework() {
        lastLaunchFindNoFireworks = -1;
    }

    private void sendCustomUseFireworkPacket(float pitch, float yaw) {
        if (thisFallFlyingIsArmorFly != -1 || thisFallFlyingIsAutoSwitch != -1) {
            delayQueue.clear();
            delayQueue.add(ItemStack.EMPTY);
        } else {
            sendUsePacket(pitch, yaw);
        }
    }

    private void sendUsePacket(float pitch, float yaw) {
        ItemStack stack = mc.player.getStackInHand(Hand.MAIN_HAND);
        if (canBeUsedAsFireworks(stack)) {
            sendUseItemPacket(Hand.MAIN_HAND, yaw, pitch);
            onHasFirework();
            return;
        }
        stack = mc.player.getStackInHand(Hand.OFF_HAND);
        if (canBeUsedAsFireworks(stack)) {
            sendUseItemPacket(Hand.OFF_HAND, yaw, pitch);
            onHasFirework();
            return;
        }
        int slot = findRocketSlot();
        if (slot != -1) {
            if (slot != mc.player.getInventory().getSelectedSlot()) mc.player.getInventory().setSelectedSlot(slot);
            sendUseItemPacket(Hand.MAIN_HAND, yaw, pitch);
            onHasFirework();
        } else {
            onNoFireworks();
        }
    }

    /** 1.21.11 中 sendSequencedPacket 为 private，自写等价实现 */
    private int packetSequence;

    private void sendUseItemPacket(Hand hand, float yaw, float pitch) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(hand, packetSequence++, yaw, pitch));
        }
    }

    private int findRocketSlot() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) if (canBeUsedAsFireworks(inv.getStack(i))) return i;
        for (int i = 9; i < 36; i++) if (canBeUsedAsFireworks(inv.getStack(i))) return i;
        return -1;
    }

    private void flushRockets() {
        flushing = true;
        try {
            int selected = mc.player.getInventory().getSelectedSlot();
            while (!delayQueue.isEmpty()) {
                ItemStack entry = delayQueue.poll();
                if (!entry.isEmpty()) {
                    int slot = findMatchingSlot(entry);
                    if (slot != -1) {
                        if (slot != mc.player.getInventory().getSelectedSlot()) mc.player.getInventory().setSelectedSlot(slot);
                        sendUseItemPacket(Hand.MAIN_HAND, lastYaw, lastPitch);
                    }
                } else {
                    sendUsePacket(lastYaw, lastPitch);
                }
            }
            mc.player.getInventory().setSelectedSlot(selected);
        } finally {
            flushing = false;
        }
    }

    private int findMatchingSlot(ItemStack target) {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (ItemStack.areItemsAndComponentsEqual(target, inv.getStack(i))) return i;
        }
        return -1;
    }

    private void clearRockets() {
        delayQueue.clear();
    }

    // ===== 自动缩放（盒限制） =====

    private Vec3d applyAxisLimit1(Vec3d motion, Vec3d rotation) {
        if (motion.lengthSquared() < 1E-6) return motion;
        if (ElytraShared.lastInWater || ElytraShared.lastInLava) return motion;
        if (motion.length() > autoRescaleAmount.get()) {
            return motion.normalize().multiply(autoRescaleAmount.get());
        }
        return motion;
    }

    /** V2 近似：垂直爬升限制 */
    private Vec3d applyAxisLimit2(Vec3d motion, Vec3d rotation) {
        if (motion.lengthSquared() < 1E-6) return motion;
        if (motion.y > yClimbCap.get()) motion = new Vec3d(motion.getX(), yClimbCap.get(), motion.getZ());
        return applyAxisLimit1(motion, rotation);
    }

    @Override
    public String getInfoString() {
        return armorFly.get() && thisFallFlyingIsArmorFly != -1 ? "胸甲飞行" : (autoRescale.get() ? "缩放" : "");
    }
}
