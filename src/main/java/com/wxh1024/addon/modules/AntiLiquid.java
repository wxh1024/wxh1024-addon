package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class AntiLiquid extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Boolean> swim=sg.add(new BoolSetting.Builder().name("surface-swim").defaultValue(true).build());
    public AntiLiquid(){super(Wxh1024Addon.WXH1024,"anti-liquid","Keep controlled movement at liquid surfaces without packet spoofing.");}
    @EventHandler private void tick(TickEvent.Post e){if(!swim.get()||mc.player==null||!mc.player.isTouchingWater())return;var v=mc.player.getVelocity();if(mc.options.jumpKey.isPressed())mc.player.setVelocity(v.x,Math.max(v.y,.08),v.z);}
}
