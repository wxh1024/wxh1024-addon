package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Meteor Tick rewrite of the portable entity-search/logging portion of EntityLog. */
public class EntitySearchNotifier extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> range = sg.add(new IntSetting.Builder().name("range")
            .defaultValue(32).min(4).sliderMax(128).build());
    private final Setting<Boolean> playersOnly = sg.add(new BoolSetting.Builder().name("players-only")
            .defaultValue(false).build());
    private final Set<UUID> seen = new HashSet<>();

    public EntitySearchNotifier() {
        super(Wxh1024Addon.WXH1024, "entity-search-notifier",
                "Reports newly observed nearby entities without the legacy event bus.");
    }

    @Override
    public void onActivate() { seen.clear(); }

    @Override
    public void onDeactivate() { seen.clear(); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        for (Entity entity : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(range.get()))) {
            if (playersOnly.get() && !(entity instanceof PlayerEntity)) continue;
            if (seen.add(entity.getUuid())) {
                info(Text.literal("发现实体: " + entity.getName().getString()
                        + " (" + entity.getBlockPos().toShortString() + ")"));
            }
        }
        seen.removeIf(uuid -> mc.world.getEntity(uuid) == null);
    }
}
