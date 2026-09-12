package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class AutoLogoutSafety extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> health = sg.add(new DoubleSetting.Builder().name("health-threshold").defaultValue(4.0).min(0).sliderMax(20).build());
    private final Setting<Integer> totems = sg.add(new IntSetting.Builder().name("minimum-totems").defaultValue(0).min(0).sliderMax(10).build());
    private final Setting<Double> minY = sg.add(new DoubleSetting.Builder().name("minimum-y").defaultValue(-64).sliderMin(-64).sliderMax(320).build());
    private int cooldown;

    public AutoLogoutSafety() { super(Wxh1024Addon.WXH1024, "auto-logout-safety", "Disconnect on configurable survival danger conditions."); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null || cooldown > 0) { if (cooldown > 0) cooldown--; return; }
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++)
            if (mc.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) count += mc.player.getInventory().getStack(i).getCount();
        if (mc.player.getHealth() <= health.get() || count < totems.get() || mc.player.getY() < minY.get()) {
            warning("安全模块触发，正在断开连接");
            mc.disconnect(Text.literal("安全模块自动断开连接"));
            cooldown = 40;
        }
    }
}
