package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class AntiChunkLag extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Integer> maxEntities=sg.add(new IntSetting.Builder().name("entity-limit").defaultValue(300).min(20).sliderMax(2000).build());
    private int cooldown;
    public AntiChunkLag(){super(Wxh1024Addon.WXH1024,"anti-chunk-lag","Warn when a loaded area contains an unusually large entity count.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.world==null||mc.player==null||cooldown>0){if(cooldown>0)cooldown--;return;}int n=mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(32)).size();if(n>maxEntities.get()){warning("附近实体数量过高: "+n);cooldown=100;}}
}
