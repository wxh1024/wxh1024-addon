package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class Sprint extends Module {
    private final SettingGroup sg=settings.getDefaultGroup();
    private final Setting<Boolean> omni=sg.add(new BoolSetting.Builder().name("omni").defaultValue(true).build());
    public Sprint(){super(Wxh1024Addon.WXH1024,"sprint","Automatically sprint while moving.");}
    @EventHandler private void tick(TickEvent.Post e){if(mc.player==null)return; boolean moving=mc.options.forwardKey.isPressed()||mc.options.backKey.isPressed()||mc.options.leftKey.isPressed()||mc.options.rightKey.isPressed(); if(moving&&(omni.get()||mc.options.forwardKey.isPressed())&&!mc.player.isSneaking()&&!mc.player.isUsingItem())mc.player.setSprinting(true);}
}
