package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
public class CombatLog extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Double> range=sg.add(new DoubleSetting.Builder().name("range").defaultValue(16).min(1).sliderMax(64).build());
 public CombatLog(){super(Wxh1024Addon.WXH1024,"combat-log","Disconnect when another player enters the configured radius.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null)return;for(PlayerEntity p:mc.world.getPlayers())if(p!=mc.player&&mc.player.squaredDistanceTo(p)<range.get()*range.get()){warning("combat log: player nearby");mc.disconnect(net.minecraft.text.Text.literal("Combat log"));break;}}
}
