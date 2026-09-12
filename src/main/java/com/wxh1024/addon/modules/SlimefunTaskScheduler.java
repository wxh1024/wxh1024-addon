package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;
import com.wxh1024.addon.data.SlimefunModelRegistry;
import com.wxh1024.addon.utils.SlimefunItemInfo;
import com.wxh1024.addon.data.SlimefunRecipeDatabase;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import com.wxh1024.addon.data.VanillaRecipeCache;

/** Meteor-native replacement for the dependency-free part of the old task manager. */
public class SlimefunTaskScheduler extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    public final Setting<Boolean> recordItems = sg.add(new BoolSetting.Builder()
            .name("record-items").description("Record item IDs seen in the main hand.")
            .defaultValue(false).build());
    public final Setting<Boolean> saveData = sg.add(new BoolSetting.Builder()
            .name("save-data").description("Keep the in-memory task history while enabled.")
            .defaultValue(true).build());
    public final Setting<Integer> interval = sg.add(new IntSetting.Builder()
            .name("scan-interval").description("Ticks between task scans.")
            .defaultValue(10).min(1).sliderMax(100).build());
    private final Deque<String> itemHistory = new ArrayDeque<>();
    private int ticks;
    private int screenScanTicks;

    public SlimefunTaskScheduler() {
        super(Wxh1024Addon.WXH1024, "slimefun-task-scheduler",
                "Records lightweight item tasks without the old external framework.");
    }

    @Override
    public void onDeactivate() {
        ticks = 0;
        if (!saveData.get()) itemHistory.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        VanillaRecipeCache.tick();
        if (++screenScanTicks >= 5) {
            screenScanTicks = 0;
            if (mc.currentScreen instanceof HandledScreen<?> handled) SlimefunRecipeDatabase.scan(handled);
        }
        if (!recordItems.get() || mc.player == null || ++ticks < interval.get()) return;
        ticks = 0;
        ItemStack stack = mc.player.getMainHandStack();
        if (!stack.isEmpty()) {
            String id = SlimefunItemInfo.id(stack);
            SlimefunModelRegistry.recordItem(id);
            if (itemHistory.size() == 100) itemHistory.removeFirst();
            if (itemHistory.isEmpty() || !itemHistory.peekLast().equals(id)) itemHistory.addLast(id);
        }
    }

    public int historySize() {
        return itemHistory.size();
    }

    public void report() {
        info(Text.literal("已记录物品任务: " + itemHistory.size() + " 条"));
    }
}
