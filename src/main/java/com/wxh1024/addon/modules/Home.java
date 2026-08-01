package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class Home extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> sendCommandEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("发送命令")
        .description("触发时是否发送命令")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("命令")
        .description("触发时执行的命令（例如 /m  或 .tp）")
        .defaultValue(".tp 8")
        .visible(() -> sendCommandEnabled.get())
        .build()
    );

    private final Setting<Boolean> sendChatMessageEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("聊天通知")
        .description("触发时是否在聊天栏显示通知")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> notifyMessage = sgGeneral.add(new StringSetting.Builder()
        .name("通知消息")
        .description("触发时发送的聊天通知内容")
        .defaultValue("[wxh1024] 自动回家已触发！")
        .visible(() -> sendChatMessageEnabled.get())
        .build()
    );

    private final Setting<Integer> minTotems = sgGeneral.add(new IntSetting.Builder()
        .name("最小图腾数")
        .description("背包中不死图腾数量低于此值时触发（设为0则不检查）")
        .defaultValue(2)
        .min(0)
        .max(36)
        .build()
    );

    private final Setting<Boolean> heightCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("高度检查")
        .description("是否检查玩家高度")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> minHeight = sgGeneral.add(new DoubleSetting.Builder()
        .name("最小高度")
        .description("玩家 Y 坐标低于此值时触发")
        .defaultValue(0.0)
        .min(-64.0)
        .max(320.0)
        .visible(() -> heightCheck.get())
        .build()
    );

    private final Setting<Boolean> toggleOffOnTrigger = sgGeneral.add(new BoolSetting.Builder()
        .name("触发后关闭")
        .description("触发后自动关闭本模块")
        .defaultValue(false)
        .build()
    );

    private boolean triggered = false;

    public Home() {
        super(
            Wxh1024Addon.WXH1024,
            "自动回家",
            "当不死图腾数量或高度低于阈值时自动执行回家命令。"
        );
    }

    @Override
    public void onActivate() {
        triggered = false;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        boolean shouldTrigger = false;
        String reason = "";

        // 检查图腾数量
        int totemCount = countTotems();
        if (minTotems.get() > 0 && totemCount < minTotems.get()) {
            shouldTrigger = true;
            reason = "图腾不足:" + totemCount + "/" + minTotems.get();
        }

        // 检查高度
        double playerY = mc.player.getY();
        if (heightCheck.get() && playerY < minHeight.get()) {
            shouldTrigger = true;
            reason = reason.isEmpty() ? "高度过低:" + String.format("%.1f", playerY)
                : reason + " & 高度过低:" + String.format("%.1f", playerY);
        }

        if (!shouldTrigger) {
            this.triggered = false;
            return;
        }

        if (!this.triggered) {
            this.triggered = true;
            triggerHome(reason);
        }
    }

    private int countTotems() {
        if (mc.player == null) return 0;
        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        return result.count();
    }

    private void triggerHome(String reason) {
        // 聊天通知
        if (sendChatMessageEnabled.get() && !notifyMessage.get().isEmpty()) {
            info(notifyMessage.get() + " 原因:" + reason);
        }

        // 发送命令
        if (sendCommandEnabled.get() && !command.get().trim().isEmpty()) {
            ChatUtils.sendPlayerMsg(command.get().trim());
        }

        // 触发后关闭
        if (toggleOffOnTrigger.get()) {
            toggle();
        }
    }

    @Override
    public String getInfoString() {
        if (mc.player == null) return "";

        int totems = countTotems();
        double y = mc.player.getY();

        return "图腾:" + totems + " 高度:" + String.format("%.1f", y);
    }
}
